package co.edu.unicauca.notification.service;

import co.edu.unicauca.notification.dto.NotificationDTO;
import co.edu.unicauca.notification.model.Notification;
import co.edu.unicauca.notification.model.NotificationType;
import co.edu.unicauca.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    public NotificationDTO notify(Long recipientId, NotificationType type, String subject, String message,
                                  Long questionId) {
        Notification notification = notificationRepository.save(
                new Notification(recipientId, type, subject, message, questionId));
        try {
            emailService.send(notification);
            notification.markAsSent();
        } catch (RuntimeException ex) {
            log.error("[NotificationService] Falló el envío de la notificación {}: {}", notification.getId(),
                    ex.getMessage());
            notification.markAsFailed();
        }
        return NotificationDTO.from(notificationRepository.save(notification));
    }

    public List<NotificationDTO> find(Long recipientId, NotificationType type) {
        return notificationRepository.findAll().stream()
                .filter(notification -> recipientId == null || recipientId.equals(notification.getRecipientId()))
                .filter(notification -> type == null || type == notification.getType())
                .sorted(Comparator.comparing(Notification::getId))
                .map(NotificationDTO::from)
                .toList();
    }

    public NotificationDTO findById(Long id) {
        return notificationRepository.findById(id).map(NotificationDTO::from)
                .orElseThrow(() -> new IllegalArgumentException("No existe la notificación " + id));
    }
}
