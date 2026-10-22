package co.edu.unicauca.lifecycle.dto;

import java.time.LocalDateTime;

/** Comando recibido desde Question Service a traves de RabbitMQ. */
public record SubmitQuestionForReviewCommand(String operationId, Long questionId, Long requestedBy,
                                             LocalDateTime requestedAt) {
}
