package co.edu.unicauca.lifecycle.service;

import co.edu.unicauca.lifecycle.dto.OperationResultDTO;
import co.edu.unicauca.lifecycle.dto.QuestionStateDTO;
import co.edu.unicauca.lifecycle.dto.RegisterQuestionRequestDTO;
import co.edu.unicauca.lifecycle.dto.StateTransitionDTO;
import co.edu.unicauca.lifecycle.dto.SubmitQuestionForReviewCommand;
import co.edu.unicauca.lifecycle.dto.TransitionRequestDTO;
import co.edu.unicauca.lifecycle.exception.BadRequestException;
import co.edu.unicauca.lifecycle.exception.ConflictException;
import co.edu.unicauca.lifecycle.exception.ResourceNotFoundException;
import co.edu.unicauca.lifecycle.model.OperationResult;
import co.edu.unicauca.lifecycle.model.OperationStatus;
import co.edu.unicauca.lifecycle.model.QuestionState;
import co.edu.unicauca.lifecycle.model.QuestionStatus;
import co.edu.unicauca.lifecycle.model.StateTransition;
import co.edu.unicauca.lifecycle.repository.OperationResultRepository;
import co.edu.unicauca.lifecycle.repository.QuestionStateRepository;
import co.edu.unicauca.lifecycle.repository.StateTransitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class LifecycleService {

    private static final Logger log = LoggerFactory.getLogger(LifecycleService.class);
    private static final String SUBMIT_FOR_REVIEW = "SUBMIT_FOR_REVIEW";

    private final QuestionStateRepository stateRepository;
    private final StateTransitionRepository transitionRepository;
    private final OperationResultRepository operationRepository;

    public LifecycleService(QuestionStateRepository stateRepository, StateTransitionRepository transitionRepository,
                            OperationResultRepository operationRepository) {
        this.stateRepository = stateRepository;
        this.transitionRepository = transitionRepository;
        this.operationRepository = operationRepository;
    }

    /** Registra el estado inicial DRAFT de una pregunta recien creada en Question Service. */
    @Transactional
    public QuestionStateDTO register(RegisterQuestionRequestDTO request) {
        if (stateRepository.existsByQuestionId(request.questionId())) {
            throw new ConflictException("La pregunta " + request.questionId() + " ya está registrada");
        }
        QuestionState state = stateRepository.save(new QuestionState(request.questionId(), request.authorId()));
        transitionRepository.save(new StateTransition(request.questionId(), null, QuestionStatus.DRAFT,
                request.authorId(), state.getComment(), null));
        log.info("[LifecycleService] Pregunta {} registrada en estado DRAFT", request.questionId());
        return QuestionStateDTO.from(state);
    }

    @Transactional(readOnly = true)
    public QuestionStateDTO getState(Long questionId) {
        return QuestionStateDTO.from(find(questionId));
    }

    @Transactional(readOnly = true)
    public List<StateTransitionDTO> history(Long questionId) {
        find(questionId);
        return transitionRepository.findByQuestionIdOrderByIdAsc(questionId).stream()
                .map(StateTransitionDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OperationResultDTO getOperation(String operationId) {
        return operationRepository.findById(operationId)
                .map(OperationResultDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La operación " + operationId + " no ha sido procesada todavía o no existe"));
    }

    /**
     * Transicion solicitada de forma sincrona (por ejemplo, desde Review Service). Lanza
     * InvalidTransitionException (409) si la maquina de estados no la permite.
     */
    @Transactional
    public QuestionStateDTO transition(Long questionId, TransitionRequestDTO request) {
        QuestionStatus target = parseState(request.targetState());
        if (target == QuestionStatus.REJECTED && (request.comment() == null || request.comment().isBlank())) {
            throw new BadRequestException("El motivo del rechazo (comment) es obligatorio");
        }
        QuestionState state = find(questionId);
        QuestionStatus previous = state.applyTransition(target, request.changedBy(), request.comment());
        transitionRepository.save(new StateTransition(questionId, previous, target, request.changedBy(),
                request.comment(), null));
        log.info("[LifecycleService] Pregunta {}: {} -> {} (usuario {})", questionId, previous, target,
                request.changedBy());
        return QuestionStateDTO.from(state);
    }

    /**
     * Procesa el comando SubmitQuestionForReview (DRAFT -> PENDING_REVIEW). Los fallos de negocio
     * no lanzan excepcion: se registran como FAILED para que el cliente pueda consultarlos.
     */
    @Transactional
    public SubmitOutcome processSubmitCommand(SubmitQuestionForReviewCommand command) {
        Optional<OperationResult> alreadyProcessed = operationRepository.findById(command.operationId());
        if (alreadyProcessed.isPresent()) {
            log.warn("[LifecycleService] Operación {} ya procesada; se ignora el duplicado", command.operationId());
            return new SubmitOutcome(alreadyProcessed.get(), null, true);
        }

        Optional<QuestionState> state = stateRepository.findByQuestionId(command.questionId());
        OperationStatus status;
        String message;
        String resultingState = null;
        Long authorId = null;
        if (state.isEmpty()) {
            status = OperationStatus.FAILED;
            message = "La pregunta " + command.questionId() + " no está registrada en Lifecycle Service";
        } else if (!state.get().canTransitionTo(QuestionStatus.PENDING_REVIEW)) {
            status = OperationStatus.FAILED;
            resultingState = state.get().getState().name();
            message = "Transición no permitida: " + resultingState + " -> PENDING_REVIEW. "
                    + "Solo una pregunta en DRAFT puede enviarse a revisión";
        } else {
            QuestionState current = state.get();
            QuestionStatus previous = current.applyTransition(QuestionStatus.PENDING_REVIEW, command.requestedBy(),
                    "Enviada a revisión por el autor");
            transitionRepository.save(new StateTransition(command.questionId(), previous,
                    QuestionStatus.PENDING_REVIEW, command.requestedBy(), current.getComment(), command.operationId()));
            status = OperationStatus.COMPLETED;
            resultingState = QuestionStatus.PENDING_REVIEW.name();
            authorId = current.getAuthorId();
            message = "Transición " + previous + " -> PENDING_REVIEW completada";
        }

        OperationResult result = operationRepository.save(new OperationResult(command.operationId(),
                command.questionId(), SUBMIT_FOR_REVIEW, status, message, resultingState, command.requestedAt()));
        log.info("[LifecycleService] Operación {} -> {}: {}", command.operationId(), status, message);
        return new SubmitOutcome(result, authorId, false);
    }

    private QuestionState find(Long questionId) {
        return stateRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La pregunta " + questionId + " no está registrada en Lifecycle Service"));
    }

    private QuestionStatus parseState(String value) {
        try {
            return QuestionStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Estado desconocido: " + value + ". Valores válidos: "
                    + List.of(QuestionStatus.values()));
        }
    }
}
