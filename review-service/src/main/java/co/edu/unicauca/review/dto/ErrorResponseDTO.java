package co.edu.unicauca.review.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message, List<String> details) {

    public static ErrorResponseDTO of(int status, String error, String message, List<String> details) {
        return new ErrorResponseDTO(LocalDateTime.now(), status, error, message, details);
    }
}
