package co.edu.unicauca.review.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_assignments")
public class ReviewAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long questionId;

    private Long reviewerId;

    private Long assignedBy;

    private LocalDateTime assignedAt;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    @Column(length = 1000)
    private String statusDetail;

    private boolean notificationPublished;

    protected ReviewAssignment() {
    }

    public ReviewAssignment(Long questionId, Long reviewerId, Long assignedBy) {
        this.questionId = questionId;
        this.reviewerId = reviewerId;
        this.assignedBy = assignedBy;
        this.assignedAt = LocalDateTime.now();
        this.status = AssignmentStatus.PENDING;
        this.statusDetail = "Esperando confirmación de Lifecycle Service";
    }

    public void confirm() {
        this.status = AssignmentStatus.ASSIGNED;
        this.statusDetail = "Pregunta en IN_REVIEW";
    }

    public void cancel(String reason) {
        this.status = AssignmentStatus.CANCELLED;
        this.statusDetail = reason;
    }

    public void complete(ReviewResult result) {
        this.status = AssignmentStatus.COMPLETED;
        this.statusDetail = "Revisión finalizada: " + result;
    }

    public void markNotificationPublished(boolean published) {
        this.notificationPublished = published;
    }

    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public String getStatusDetail() {
        return statusDetail;
    }

    public boolean isNotificationPublished() {
        return notificationPublished;
    }
}
