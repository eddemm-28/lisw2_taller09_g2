package co.edu.unicauca.question.dto;

import co.edu.unicauca.question.model.Question;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Pregunta con su estado actual. El estado se consulta a Lifecycle Service (autoridad);
 * si no esta disponible se informa UNKNOWN.
 */
public record QuestionDTO(Long id, String text, List<OptionDTO> options, String correctOptionId, Long authorId,
                          String status, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static QuestionDTO from(Question question, String status) {
        List<OptionDTO> options = question.getOptions().stream()
                .map(option -> new OptionDTO(option.getOptionId(), option.getText()))
                .toList();
        return new QuestionDTO(question.getId(), question.getText(), options, question.getCorrectOptionId(),
                question.getAuthorId(), status, question.getCreatedAt(), question.getUpdatedAt());
    }
}
