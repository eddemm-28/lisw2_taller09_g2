package co.edu.unicauca.review.messaging;

import co.edu.unicauca.review.config.RabbitMQConfig;
import co.edu.unicauca.review.dto.QuestionReviewedEvent;
import co.edu.unicauca.review.dto.ReviewerAssignedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica eventos de revision. Un fallo al publicar no revierte la asignacion: la notificacion
 * no debe bloquear la operacion principal.
 */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean publishReviewerAssigned(ReviewerAssignedEvent event) {
        return publish(RabbitMQConfig.ROUTING_KEY_REVIEWER_ASSIGNED, event, "ReviewerAssigned");
    }

    public boolean publishQuestionReviewed(QuestionReviewedEvent event) {
        return publish(RabbitMQConfig.ROUTING_KEY_QUESTION_REVIEWED, event, "QuestionReviewed");
    }

    private boolean publish(String routingKey, Object event, String eventName) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
            log.info("[ReviewService] Evento {} publicado (routing key {})", eventName, routingKey);
            return true;
        } catch (AmqpException ex) {
            log.warn("[ReviewService] No se pudo publicar {}: {}", eventName, ex.getMessage());
            return false;
        }
    }
}
