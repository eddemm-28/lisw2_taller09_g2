package co.edu.unicauca.lifecycle.dto;

import jakarta.validation.constraints.NotBlank;

/** Solicitud de transicion. comment es obligatorio al rechazar (motivo del rechazo). */
public record TransitionRequestDTO(
        @NotBlank(message = "targetState es obligatorio") String targetState,
        Long changedBy,
        String comment) {
}
