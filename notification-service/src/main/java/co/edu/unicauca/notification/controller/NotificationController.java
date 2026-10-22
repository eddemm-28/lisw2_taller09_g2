package co.edu.unicauca.notification.controller;

import co.edu.unicauca.notification.dto.NotificationDTO;
import co.edu.unicauca.notification.model.NotificationType;
import co.edu.unicauca.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Consulta de las notificaciones procesadas: permite demostrar desde Postman el resultado de los
 * eventos asincronos.
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> find(@RequestParam(required = false) Long recipientId,
                                                      @RequestParam(required = false) NotificationType type) {
        return ResponseEntity.ok(notificationService.find(recipientId, type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.findById(id));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(IllegalArgumentException ex) {
        return ResponseEntity.status(404).body(Map.of("status", 404, "message", ex.getMessage()));
    }
}
