package co.edu.unicauca.notification.service;

import co.edu.unicauca.notification.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Simulacion del envio de correo: tarda un tiempo configurable, como lo haria un servidor SMTP.
 * Como se ejecuta en el hilo del listener, ese tiempo nunca lo percibe quien publico el evento.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final long sendDelayMs;

    public EmailService(@Value("${notification.simulated-send-delay-ms:0}") long sendDelayMs) {
        this.sendDelayMs = sendDelayMs;
    }

    public void send(Notification notification) {
        try {
            Thread.sleep(sendDelayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Envío interrumpido", ex);
        }
        log.info("[EMAIL SIMULADO] Para: usuario #{} | Asunto: {} | {}", notification.getRecipientId(),
                notification.getSubject(), notification.getMessage());
    }
}
