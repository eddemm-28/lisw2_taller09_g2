package co.edu.unicauca.question.messaging;

import co.edu.unicauca.question.config.RabbitMQConfig;
import co.edu.unicauca.question.dto.SubmitQuestionForReviewCommand;
import co.edu.unicauca.question.dto.ValidationFailedEvent;
import co.edu.unicauca.question.repository.OperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica comandos y eventos en RabbitMQ mediante RabbitTemplate.
 */
@Component
public class CommandPublisher {

    private static final Logger log = LoggerFactory.getLogger(CommandPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final OperationRepository operationRepository;

    public CommandPublisher(RabbitTemplate rabbitTemplate, OperationRepository operationRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.operationRepository = operationRepository;
        // Con mandatory=true, RabbitMQ devuelve los mensajes que no llegan a ninguna cola.
        this.rabbitTemplate.setReturnsCallback(this::onReturned);
    }

    /** Lanza AmqpException si el broker no esta disponible (el llamador responde 503). */
    public void publishSubmitCommand(SubmitQuestionForReviewCommand command) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_SUBMIT, command,
                message -> {
                    message.getMessageProperties().setCorrelationId(command.operationId());
                    message.getMessageProperties().setMessageId(command.operationId());
                    return message;
                });
        log.info("[QuestionService] Comando SubmitQuestionForReview publicado: pregunta={} operationId={}",
                command.questionId(), command.operationId());
    }

    /** La notificacion es secundaria: si falla la publicacion solo se registra en el log. */
    public void publishValidationFailed(ValidationFailedEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_VALIDATION_FAILED, event);
            log.info("[QuestionService] Evento ValidationFailed publicado para el autor {}", event.authorId());
        } catch (AmqpException ex) {
            log.warn("[QuestionService] No se pudo publicar ValidationFailed: {}", ex.getMessage());
        }
    }

    private void onReturned(ReturnedMessage returned) {
        String operationId = returned.getMessage().getMessageProperties().getCorrelationId();
        log.error("[QuestionService] Mensaje no enrutable (routingKey={}, motivo={})",
                returned.getRoutingKey(), returned.getReplyText());
        if (operationId == null) {
            return;
        }
        operationRepository.findById(operationId).ifPresent(operation -> {
            operation.fail("El comando no llegó a ninguna cola de RabbitMQ (routing key '" + returned.getRoutingKey()
                    + "'). Verifique que Lifecycle Service haya sido iniciado.");
            operationRepository.save(operation);
        });
    }
}
