package co.edu.unicauca.review.dto;

import java.time.LocalDateTime;

/** Evento publicado cuando la asignacion queda confirmada (consumido por Notification Service). */
public record ReviewerAssignedEvent(Long assignmentId, Long questionId, Long reviewerId, Long assignedBy,
                                    LocalDateTime occurredAt) {
}
