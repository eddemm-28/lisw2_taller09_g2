package co.edu.unicauca.question.service;

import co.edu.unicauca.question.client.LifecycleClient;
import co.edu.unicauca.question.client.UserClient;
import co.edu.unicauca.question.client.ValidationClient;
import co.edu.unicauca.question.dto.CreateQuestionRequestDTO;
import co.edu.unicauca.question.dto.CreateQuestionResponseDTO;
import co.edu.unicauca.question.dto.LifecycleStateDTO;
import co.edu.unicauca.question.dto.OptionDTO;
import co.edu.unicauca.question.dto.QuestionDTO;
import co.edu.unicauca.question.dto.SubmitAcceptedDTO;
import co.edu.unicauca.question.dto.SubmitQuestionForReviewCommand;
import co.edu.unicauca.question.dto.UserDTO;
import co.edu.unicauca.question.dto.ValidationFailedEvent;
import co.edu.unicauca.question.dto.ValidationRequestDTO;
import co.edu.unicauca.question.dto.ValidationResultDTO;
import co.edu.unicauca.question.exception.ConflictException;
import co.edu.unicauca.question.exception.ForbiddenException;
import co.edu.unicauca.question.exception.QuestionValidationException;
import co.edu.unicauca.question.exception.ResourceNotFoundException;
import co.edu.unicauca.question.exception.ServiceUnavailableException;
import co.edu.unicauca.question.messaging.CommandPublisher;
import co.edu.unicauca.question.model.Operation;
import co.edu.unicauca.question.model.Option;
import co.edu.unicauca.question.model.Question;
import co.edu.unicauca.question.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);
    private static final Set<String> AUTHOR_ROLES = Set.of("TEACHER", "AUTHOR");
    private static final Set<String> EDITABLE_STATES = Set.of("DRAFT", "REJECTED");
    private static final String UNKNOWN_STATE = "UNKNOWN";

    private final QuestionRepository questionRepository;
    private final OperationService operationService;
    private final UserClient userClient;
    private final ValidationClient validationClient;
    private final LifecycleClient lifecycleClient;
    private final CommandPublisher commandPublisher;

    public QuestionService(QuestionRepository questionRepository, OperationService operationService,
                           UserClient userClient, ValidationClient validationClient,
                           LifecycleClient lifecycleClient, CommandPublisher commandPublisher) {
        this.questionRepository = questionRepository;
        this.operationService = operationService;
        this.userClient = userClient;
        this.validationClient = validationClient;
        this.lifecycleClient = lifecycleClient;
        this.commandPublisher = commandPublisher;
    }

    /**
     * Escenario 1, pasos 2 y 3: creacion y validacion sincronas. Si Lifecycle Service no puede
     * registrar el estado inicial, la transaccion se revierte y la pregunta no queda guardada.
     */
    @Transactional
    public CreateQuestionResponseDTO create(String authorization, CreateQuestionRequestDTO request) {
        UserDTO author = userClient.validateToken(authorization);
        if (!author.hasAnyRole(AUTHOR_ROLES)) {
            throw new ForbiddenException("Solo un docente o autor puede crear preguntas");
        }
        validateOrFail(request, author);

        Question question = questionRepository.save(new Question(request.text().trim(), toOptions(request.options()),
                request.correctOptionId().trim(), author.id()));
        lifecycleClient.registerDraft(question.getId(), author.id());
        log.info("[QuestionService] Pregunta {} creada por {} en estado DRAFT", question.getId(), author.username());
        return new CreateQuestionResponseDTO(question.getId(), "DRAFT",
                "Pregunta validada y registrada en estado DRAFT");
    }

    @Transactional(readOnly = true)
    public QuestionDTO findById(Long id) {
        return toDTO(find(id));
    }

    @Transactional(readOnly = true)
    public List<QuestionDTO> findAll() {
        return questionRepository.findAll().stream().map(this::toDTO).toList();
    }

    /**
     * Correccion de una pregunta rechazada (o en borrador). Se vuelve a validar y, si estaba
     * REJECTED, se solicita a Lifecycle Service la transicion REJECTED -> DRAFT para que pueda
     * reenviarse a revision.
     */
    @Transactional
    public QuestionDTO correct(String authorization, Long id, CreateQuestionRequestDTO request) {
        UserDTO user = userClient.validateToken(authorization);
        Question question = find(id);
        requireAuthor(user, question);

        LifecycleStateDTO current = lifecycleClient.getState(id);
        if (!EDITABLE_STATES.contains(current.state())) {
            throw new ConflictException("Solo se pueden corregir preguntas en estado DRAFT o REJECTED (estado actual: "
                    + current.state() + ")");
        }
        validateOrFail(request, user);
        question.updateContent(request.text().trim(), toOptions(request.options()), request.correctOptionId().trim());

        String newState = current.state();
        if ("REJECTED".equals(current.state())) {
            newState = lifecycleClient.requestTransition(id, "DRAFT", user.id(),
                    "Pregunta corregida por el autor después del rechazo").state();
        }
        log.info("[QuestionService] Pregunta {} corregida; estado actual {}", id, newState);
        return QuestionDTO.from(question, newState);
    }

    /**
     * Escenario 1, pasos 4 y 5: publica el comando SubmitQuestionForReview y responde de inmediato
     * con el identificador de la operacion, sin esperar a Lifecycle Service.
     */
    public SubmitAcceptedDTO submitForReview(String authorization, Long id) {
        UserDTO user = userClient.validateToken(authorization);
        Question question = find(id);
        requireAuthor(user, question);

        String operationId = UUID.randomUUID().toString();
        Operation operation = operationService.register(
                new Operation(operationId, id, "SUBMIT_FOR_REVIEW", user.id()));
        try {
            commandPublisher.publishSubmitCommand(
                    new SubmitQuestionForReviewCommand(operationId, id, user.id(), LocalDateTime.now()));
        } catch (AmqpException ex) {
            operation.fail("No fue posible publicar el comando en RabbitMQ: " + ex.getMessage());
            operationService.register(operation);
            throw new ServiceUnavailableException("RabbitMQ no está disponible: la solicitud de envío a revisión "
                    + "no fue aceptada (operationId " + operationId + ")");
        }
        return new SubmitAcceptedDTO("Solicitud de envío a revisión aceptada para procesamiento.", operationId,
                "PROCESSING", "/operations/" + operationId);
    }

    private void validateOrFail(CreateQuestionRequestDTO request, UserDTO author) {
        ValidationResultDTO result = validationClient.validate(
                new ValidationRequestDTO(request.text(), request.options(), request.correctOptionId()));
        if (!result.valid()) {
            log.info("[QuestionService] Pregunta rechazada por Validation Service: {}", result.errors());
            commandPublisher.publishValidationFailed(new ValidationFailedEvent(UUID.randomUUID().toString(), null,
                    author.id(), abbreviate(request.text()), result.errors(), LocalDateTime.now()));
            throw new QuestionValidationException(result.errors());
        }
    }

    private Question find(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la pregunta con id " + id));
    }

    private void requireAuthor(UserDTO user, Question question) {
        if (!question.isAuthoredBy(user.id())) {
            throw new ForbiddenException("Solo el autor de la pregunta puede realizar esta operación");
        }
    }

    private QuestionDTO toDTO(Question question) {
        String state = lifecycleClient.findState(question.getId()).map(LifecycleStateDTO::state).orElse(UNKNOWN_STATE);
        return QuestionDTO.from(question, state);
    }

    private static List<Option> toOptions(List<OptionDTO> options) {
        return options.stream().map(option -> new Option(option.id().trim(), option.text().trim())).toList();
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 80 ? text : text.substring(0, 77) + "...";
    }
}
