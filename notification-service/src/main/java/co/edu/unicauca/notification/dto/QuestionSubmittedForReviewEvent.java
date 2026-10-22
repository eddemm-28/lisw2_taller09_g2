package co.edu.unicauca.notification.dto;

import java.time.LocalDateTime;

public record QuestionSubmittedForReviewEvent(String operationId, Long questionId, Long authorId,
                                              String newState, LocalDateTime occurredAt) {
}
