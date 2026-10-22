package co.edu.unicauca.question.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Estado de la pregunta segun Lifecycle Service. */
public record LifecycleStateDTO(Long questionId, Long authorId, String state, List<String> allowedTransitions,
                                LocalDateTime changedAt, Long changedBy, String comment) {
}
