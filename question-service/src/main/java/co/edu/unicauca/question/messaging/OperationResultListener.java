package co.edu.unicauca.question.messaging;

import co.edu.unicauca.question.config.RabbitMQConfig;
import co.edu.unicauca.question.dto.OperationResultEvent;
import co.edu.unicauca.question.service.OperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume el resultado de las operaciones asincronas para que el cliente pueda consultarlo
 * con GET /operations/{operationId}.
 */
@Component
public class OperationResultListener {

    private static final Logger log = LoggerFactory.getLogger(OperationResultListener.class);

    private final OperationService operationService;

    public OperationResultListener(OperationService operationService) {
        this.operationService = operationService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_OPERATION_RESULT)
    public void onOperationResult(OperationResultEvent event) {
        log.info("[QuestionService] Resultado recibido: operationId={} status={} nuevoEstado={}",
                event.operationId(), event.status(), event.newState());
        operationService.applyResult(event);
    }
}
