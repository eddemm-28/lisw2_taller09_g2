package co.edu.unicauca.question.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Evento publicado cuando una pregunta no supera la validacion (consumido por Notification Service). */
public record ValidationFailedEvent(String operationId, Long questionId, Long authorId, String questionText,
                                    List<String> errors, LocalDateTime occurredAt) {
}
