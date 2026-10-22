package co.edu.unicauca.lifecycle.messaging;

import co.edu.unicauca.lifecycle.config.RabbitMQConfig;
import co.edu.unicauca.lifecycle.dto.OperationResultEvent;
import co.edu.unicauca.lifecycle.dto.QuestionSubmittedForReviewEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /** Resultado que consulta el cliente; si falla la publicacion, el comando se reintenta. */
    public void publishOperationResult(OperationResultEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_OPERATION_RESULT, event);
        log.info("[LifecycleService] OperationResult publicado: operationId={} status={}",
                event.operationId(), event.status());
    }

    public void publishQuestionSubmitted(QuestionSubmittedForReviewEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_SUBMITTED, event);
            log.info("[LifecycleService] Evento QuestionSubmittedForReview publicado: pregunta={}", event.questionId());
        } catch (AmqpException ex) {
            log.warn("[LifecycleService] No se pudo publicar QuestionSubmittedForReview: {}", ex.getMessage());
        }
    }
}
