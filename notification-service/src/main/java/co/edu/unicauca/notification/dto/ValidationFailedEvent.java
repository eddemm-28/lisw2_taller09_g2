package co.edu.unicauca.notification.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationFailedEvent(String operationId, Long questionId, Long authorId, String questionText,
                                    List<String> errors, LocalDateTime occurredAt) {
}
