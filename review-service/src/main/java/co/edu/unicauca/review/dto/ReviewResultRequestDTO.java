package co.edu.unicauca.review.dto;

import co.edu.unicauca.review.model.ReviewResult;
import jakarta.validation.constraints.NotNull;

/** Resultado de la revision. comments es obligatorio cuando result = REJECTED (motivo). */
public record ReviewResultRequestDTO(
        @NotNull(message = "questionId es obligatorio") Long questionId,
        @NotNull(message = "reviewerId es obligatorio") Long reviewerId,
        @NotNull(message = "result es obligatorio (APPROVED o REJECTED)") ReviewResult result,
        String comments) {
}
