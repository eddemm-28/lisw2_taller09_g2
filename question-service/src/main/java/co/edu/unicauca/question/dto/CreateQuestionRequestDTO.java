package co.edu.unicauca.question.dto;

import java.util.List;

/**
 * Contenido de la pregunta enviado por el docente. La validacion estructural la realiza
 * Validation Service de forma sincrona.
 */
public record CreateQuestionRequestDTO(String text, List<OptionDTO> options, String correctOptionId) {
}
