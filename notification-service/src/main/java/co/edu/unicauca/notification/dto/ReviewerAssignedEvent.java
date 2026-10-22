package co.edu.unicauca.notification.dto;

import java.time.LocalDateTime;

public record ReviewerAssignedEvent(Long assignmentId, Long questionId, Long reviewerId, Long assignedBy,
                                    LocalDateTime occurredAt) {
}
