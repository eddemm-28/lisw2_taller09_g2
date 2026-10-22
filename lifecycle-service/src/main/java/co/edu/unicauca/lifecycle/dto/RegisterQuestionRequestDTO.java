package co.edu.unicauca.lifecycle.dto;

import jakarta.validation.constraints.NotNull;

public record RegisterQuestionRequestDTO(
        @NotNull(message = "questionId es obligatorio") Long questionId,
        @NotNull(message = "authorId es obligatorio") Long authorId) {
}
