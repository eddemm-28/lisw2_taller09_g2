package co.edu.unicauca.question;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Question Service (:8083). Administra el contenido de las preguntas y coordina el flujo:
 * REST sincrono hacia User, Validation y Lifecycle Service, y publicacion del comando
 * SubmitQuestionForReview en RabbitMQ.
 */
@SpringBootApplication
public class QuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestionServiceApplication.class, args);
    }
}
