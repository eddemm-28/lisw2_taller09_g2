package co.edu.unicauca.lifecycle.dto;

import java.time.LocalDateTime;

/** Evento de dominio: la pregunta quedo en PENDING_REVIEW. */
public record QuestionSubmittedForReviewEvent(String operationId, Long questionId, Long authorId,
                                              String newState, LocalDateTime occurredAt) {
}
