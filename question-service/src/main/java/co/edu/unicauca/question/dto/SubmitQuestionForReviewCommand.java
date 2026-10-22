package co.edu.unicauca.question.dto;

import java.time.LocalDateTime;

/**
 * Comando asincrono dirigido a Lifecycle Service: solicita la transicion DRAFT -> PENDING_REVIEW.
 */
public record SubmitQuestionForReviewCommand(String operationId, Long questionId, Long requestedBy,
                                             LocalDateTime requestedAt) {
}
