package co.edu.unicauca.notification.dto;

import java.time.LocalDateTime;

public record QuestionReviewedEvent(Long questionId, Long reviewerId, Long authorId, String result, String comments,
                                    LocalDateTime occurredAt) {
}
