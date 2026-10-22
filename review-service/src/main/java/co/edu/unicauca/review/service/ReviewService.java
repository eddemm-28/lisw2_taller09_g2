package co.edu.unicauca.review.service;

import co.edu.unicauca.review.client.LifecycleClient;
import co.edu.unicauca.review.client.UserClient;
import co.edu.unicauca.review.dto.AssignReviewerRequestDTO;
import co.edu.unicauca.review.dto.AssignmentDTO;
import co.edu.unicauca.review.dto.QuestionReviewedEvent;
import co.edu.unicauca.review.dto.QuestionStateDTO;
import co.edu.unicauca.review.dto.ReviewDTO;
import co.edu.unicauca.review.dto.ReviewResultRequestDTO;
import co.edu.unicauca.review.dto.ReviewerAssignedEvent;
import co.edu.unicauca.review.dto.UserDTO;
import co.edu.unicauca.review.exception.BadRequestException;
import co.edu.unicauca.review.exception.ConflictException;
import co.edu.unicauca.review.exception.ForbiddenException;
import co.edu.unicauca.review.exception.ResourceNotFoundException;
import co.edu.unicauca.review.messaging.EventPublisher;
import co.edu.unicauca.review.model.AssignmentStatus;
import co.edu.unicauca.review.model.Review;
import co.edu.unicauca.review.model.ReviewAssignment;
import co.edu.unicauca.review.model.ReviewResult;
import co.edu.unicauca.review.repository.ReviewAssignmentRepository;
import co.edu.unicauca.review.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    private static final String PENDING_REVIEW = "PENDING_REVIEW";
    private static final String IN_REVIEW = "IN_REVIEW";

    private final ReviewAssignmentRepository assignmentRepository;
    private final ReviewRepository reviewRepository;
    private final UserClient userClient;
    private final LifecycleClient lifecycleClient;
    private final EventPublisher eventPublisher;

    public ReviewService(ReviewAssignmentRepository assignmentRepository, ReviewRepository reviewRepository,
                         UserClient userClient, LifecycleClient lifecycleClient, EventPublisher eventPublisher) {
        this.assignmentRepository = assignmentRepository;
        this.reviewRepository = reviewRepository;
        this.userClient = userClient;
        this.lifecycleClient = lifecycleClient;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Escenario 1, pasos 7 y 8. La asignacion se registra como PENDING, se solicita a Lifecycle
     * Service la transicion PENDING_REVIEW -> IN_REVIEW y solo si se confirma queda ASSIGNED; si
     * falla se compensa (CANCELLED) para no dejar una asignacion activa con un estado inconsistente.
     */
    public AssignmentDTO assignReviewer(AssignReviewerRequestDTO request) {
        UserDTO admin = userClient.getUser(request.assignedBy());
        if (!admin.hasRole("ADMIN")) {
            throw new ForbiddenException("Solo un administrador puede asignar revisores (usuario "
                    + admin.username() + ")");
        }
        UserDTO reviewer = userClient.getUser(request.reviewerId());
        if (!reviewer.active() || !reviewer.hasRole("REVIEWER")) {
            throw new BadRequestException("El usuario " + reviewer.username() + " no es un revisor activo");
        }
        QuestionStateDTO question = lifecycleClient.getState(request.questionId());
        if (!PENDING_REVIEW.equals(question.state())) {
            throw new ConflictException("La pregunta " + request.questionId() + " no es elegible para asignación: "
                    + "su estado es " + question.state() + " y se requiere PENDING_REVIEW");
        }
        if (reviewer.id().equals(question.authorId())) {
            throw new BadRequestException("El revisor no puede ser el autor de la pregunta");
        }
        if (assignmentRepository.existsByQuestionIdAndStatusIn(request.questionId(),
                EnumSet.of(AssignmentStatus.PENDING, AssignmentStatus.ASSIGNED))) {
            throw new ConflictException("La pregunta " + request.questionId() + " ya tiene un revisor asignado");
        }

        ReviewAssignment assignment = assignmentRepository.save(
                new ReviewAssignment(request.questionId(), reviewer.id(), admin.id()));
        try {
            lifecycleClient.requestTransition(request.questionId(), IN_REVIEW, admin.id(),
                    "Revisor asignado: " + reviewer.username());
        } catch (RuntimeException ex) {
            assignment.cancel("Compensada: " + ex.getMessage());
            assignmentRepository.save(assignment);
            log.warn("[ReviewService] Asignación {} cancelada (compensación): {}", assignment.getId(), ex.getMessage());
            throw ex;
        }
        assignment.confirm();

        boolean published = eventPublisher.publishReviewerAssigned(new ReviewerAssignedEvent(assignment.getId(),
                request.questionId(), reviewer.id(), admin.id(), LocalDateTime.now()));
        assignment.markNotificationPublished(published);
        assignment = assignmentRepository.save(assignment);
        log.info("[ReviewService] Revisor {} asignado a la pregunta {}", reviewer.username(), request.questionId());
        return AssignmentDTO.from(assignment);
    }

    /**
     * Registra el resultado de la revision y solicita la transicion IN_REVIEW -> APPROVED/REJECTED.
     * El motivo es obligatorio al rechazar.
     */
    public ReviewDTO registerResult(ReviewResultRequestDTO request) {
        if (request.result() == ReviewResult.REJECTED && (request.comments() == null || request.comments().isBlank())) {
            throw new BadRequestException("El motivo del rechazo (comments) es obligatorio");
        }
        ReviewAssignment assignment = assignmentRepository.findFirstByQuestionIdAndReviewerIdAndStatus(
                        request.questionId(), request.reviewerId(), AssignmentStatus.ASSIGNED)
                .orElseThrow(() -> new ConflictException("El revisor " + request.reviewerId()
                        + " no tiene una asignación activa para la pregunta " + request.questionId()));
        QuestionStateDTO question = lifecycleClient.getState(request.questionId());
        if (!IN_REVIEW.equals(question.state())) {
            throw new ConflictException("La pregunta " + request.questionId() + " está en " + question.state()
                    + "; solo se puede aprobar o rechazar una pregunta IN_REVIEW");
        }

        QuestionStateDTO newState = lifecycleClient.requestTransition(request.questionId(), request.result().name(),
                request.reviewerId(), request.comments());
        Review review = reviewRepository.save(new Review(assignment.getId(), request.questionId(),
                request.reviewerId(), request.result(), request.comments()));
        assignment.complete(request.result());
        assignmentRepository.save(assignment);

        eventPublisher.publishQuestionReviewed(new QuestionReviewedEvent(request.questionId(), request.reviewerId(),
                question.authorId(), request.result().name(), request.comments(), LocalDateTime.now()));
        log.info("[ReviewService] Pregunta {} revisada: {}", request.questionId(), request.result());
        return ReviewDTO.from(review, newState.state());
    }

    public List<AssignmentDTO> assignments(Long questionId) {
        List<ReviewAssignment> assignments = questionId == null
                ? assignmentRepository.findAll()
                : assignmentRepository.findByQuestionIdOrderByIdAsc(questionId);
        return assignments.stream().map(AssignmentDTO::from).toList();
    }

    public AssignmentDTO assignment(Long id) {
        return assignmentRepository.findById(id)
                .map(AssignmentDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la asignación " + id));
    }

    public List<ReviewDTO> reviews(Long questionId) {
        List<Review> reviews = questionId == null
                ? reviewRepository.findAll()
                : reviewRepository.findByQuestionIdOrderByIdAsc(questionId);
        return reviews.stream().map(review -> ReviewDTO.from(review, null)).toList();
    }
}
