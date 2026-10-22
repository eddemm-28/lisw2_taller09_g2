package co.edu.unicauca.review.dto;

import co.edu.unicauca.review.model.ReviewAssignment;

import java.time.LocalDateTime;

public record AssignmentDTO(Long id, Long questionId, Long reviewerId, Long assignedBy, LocalDateTime assignedAt,
                            String status, String statusDetail, boolean notificationPublished) {

    public static AssignmentDTO from(ReviewAssignment assignment) {
        return new AssignmentDTO(assignment.getId(), assignment.getQuestionId(), assignment.getReviewerId(),
                assignment.getAssignedBy(), assignment.getAssignedAt(), assignment.getStatus().name(),
                assignment.getStatusDetail(), assignment.isNotificationPublished());
    }
}
