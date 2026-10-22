package co.edu.unicauca.review.dto;

import java.time.LocalDateTime;

/** Evento publicado al registrar el resultado de la revision (notifica al autor). */
public record QuestionReviewedEvent(Long questionId, Long reviewerId, Long authorId, String result, String comments,
                                    LocalDateTime occurredAt) {
}
