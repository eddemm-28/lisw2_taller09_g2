package co.edu.unicauca.review.dto;

import java.time.LocalDateTime;
import java.util.List;

public record QuestionStateDTO(Long questionId, Long authorId, String state, List<String> allowedTransitions,
                               LocalDateTime changedAt, Long changedBy, String comment) {
}
