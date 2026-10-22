package co.edu.unicauca.lifecycle.messaging;

import co.edu.unicauca.lifecycle.config.RabbitMQConfig;
import co.edu.unicauca.lifecycle.dto.OperationResultEvent;
import co.edu.unicauca.lifecycle.dto.QuestionSubmittedForReviewEvent;
import co.edu.unicauca.lifecycle.dto.SubmitQuestionForReviewCommand;
import co.edu.unicauca.lifecycle.model.OperationResult;
import co.edu.unicauca.lifecycle.model.OperationStatus;
import co.edu.unicauca.lifecycle.service.LifecycleService;
import co.edu.unicauca.lifecycle.service.SubmitOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Consumidor asincrono del comando SubmitQuestionForReview (Escenario 1, paso 6).
 */
@Component
public class CommandListener {

    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

    private final LifecycleService lifecycleService;
    private final EventPublisher eventPublisher;
    private final long processingDelayMs;

    public CommandListener(LifecycleService lifecycleService, EventPublisher eventPublisher,
                           @Value("${lifecycle.simulated-processing-delay-ms:0}") long processingDelayMs) {
        this.lifecycleService = lifecycleService;
        this.eventPublisher = eventPublisher;
        this.processingDelayMs = processingDelayMs;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SUBMIT_REVIEW)
    public void handleSubmitForReview(SubmitQuestionForReviewCommand command) throws InterruptedException {
        log.info("[LifecycleService] Recibido comando SubmitQuestionForReview para pregunta ID: {} (operationId={})",
                command.questionId(), command.operationId());
        // Simula un procesamiento costoso para evidenciar que el 202 se respondio antes de terminar
        Thread.sleep(processingDelayMs);

        SubmitOutcome outcome = lifecycleService.processSubmitCommand(command);
        OperationResult result = outcome.result();
        eventPublisher.publishOperationResult(new OperationResultEvent(result.getId(), result.getQuestionId(),
                result.getStatus().name(), result.getResultingState(), result.getMessage(), LocalDateTime.now()));

        if (!outcome.duplicate() && result.getStatus() == OperationStatus.COMPLETED) {
            eventPublisher.publishQuestionSubmitted(new QuestionSubmittedForReviewEvent(result.getId(),
                    result.getQuestionId(), outcome.authorId(), result.getResultingState(), LocalDateTime.now()));
        }
    }
}
