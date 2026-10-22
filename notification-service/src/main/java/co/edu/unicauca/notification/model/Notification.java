package co.edu.unicauca.notification.model;

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
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long recipientId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String subject;

    @Column(length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private Long questionId;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    protected Notification() {
    }

    public Notification(Long recipientId, NotificationType type, String subject, String message, Long questionId) {
        this.recipientId = recipientId;
        this.type = type;
        this.subject = subject;
        this.message = message;
        this.questionId = questionId;
        this.status = NotificationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
    }

    public Long getId() {
        return id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
