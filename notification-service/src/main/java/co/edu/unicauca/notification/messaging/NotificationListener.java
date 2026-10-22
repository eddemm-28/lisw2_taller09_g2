package co.edu.unicauca.notification.messaging;

import co.edu.unicauca.notification.config.RabbitMQConfig;
import co.edu.unicauca.notification.dto.QuestionReviewedEvent;
import co.edu.unicauca.notification.dto.QuestionSubmittedForReviewEvent;
import co.edu.unicauca.notification.dto.ReviewerAssignedEvent;
import co.edu.unicauca.notification.dto.ValidationFailedEvent;
import co.edu.unicauca.notification.model.NotificationType;
import co.edu.unicauca.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidores asincronos de los eventos de dominio que generan notificaciones.
 */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_REVIEWER_ASSIGNED)
    public void onReviewerAssigned(ReviewerAssignedEvent event) {
        log.info("[NotificationService] Evento ReviewerAssigned recibido: pregunta={} revisor={}",
                event.questionId(), event.reviewerId());
        notificationService.notify(event.reviewerId(), NotificationType.REVIEWER_ASSIGNED,
                "Nueva pregunta asignada para revisión",
                "Se le asignó la pregunta #" + event.questionId() + " para revisión por pares (asignación #"
                        + event.assignmentId() + ").",
                event.questionId());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_QUESTION_REVIEWED)
    public void onQuestionReviewed(QuestionReviewedEvent event) {
        log.info("[NotificationService] Evento QuestionReviewed recibido: pregunta={} resultado={}",
                event.questionId(), event.result());
        boolean approved = "APPROVED".equals(event.result());
        String message = approved
                ? "Su pregunta #" + event.questionId() + " fue aprobada por el revisor."
                : "Su pregunta #" + event.questionId() + " fue rechazada. Motivo: " + event.comments();
        notificationService.notify(event.authorId(),
                approved ? NotificationType.QUESTION_APPROVED : NotificationType.QUESTION_REJECTED,
                approved ? "Pregunta aprobada" : "Pregunta rechazada", message, event.questionId());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_VALIDATION_FAILED)
    public void onValidationFailed(ValidationFailedEvent event) {
        log.info("[NotificationService] Evento ValidationFailed recibido: autor={} errores={}",
                event.authorId(), event.errors().size());
        notificationService.notify(event.authorId(), NotificationType.VALIDATION_FAILED,
                "La pregunta no superó la validación",
                "La pregunta \"" + event.questionText() + "\" no fue registrada. Errores: "
                        + String.join("; ", event.errors()),
                event.questionId());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_QUESTION_SUBMITTED)
    public void onQuestionSubmitted(QuestionSubmittedForReviewEvent event) {
        log.info("[NotificationService] Evento QuestionSubmittedForReview recibido: pregunta={}", event.questionId());
        notificationService.notify(event.authorId(), NotificationType.QUESTION_SUBMITTED,
                "Pregunta enviada a revisión",
                "Su pregunta #" + event.questionId() + " quedó en estado " + event.newState()
                        + " y espera la asignación de un revisor.",
                event.questionId());
    }
}
