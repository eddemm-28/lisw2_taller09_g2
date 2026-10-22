package co.edu.unicauca.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Validation Service (:8082). Valida la estructura de una pregunta de seleccion multiple con
 * unica respuesta (enunciado, 4 opciones y clave). No tiene estado: solo aplica reglas.
 */
@SpringBootApplication
public class ValidationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidationServiceApplication.class, args);
    }
}
