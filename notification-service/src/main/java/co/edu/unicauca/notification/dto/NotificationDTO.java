package co.edu.unicauca.notification.dto;

import co.edu.unicauca.notification.model.Notification;

import java.time.LocalDateTime;

public record NotificationDTO(Long id, Long recipientId, String type, String subject, String message, String status,
                              Long questionId, LocalDateTime createdAt, LocalDateTime sentAt) {

    public static NotificationDTO from(Notification notification) {
        return new NotificationDTO(notification.getId(), notification.getRecipientId(),
                notification.getType().name(), notification.getSubject(), notification.getMessage(),
                notification.getStatus().name(), notification.getQuestionId(), notification.getCreatedAt(),
                notification.getSentAt());
    }
}
