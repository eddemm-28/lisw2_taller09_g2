package co.edu.unicauca.review.dto;

public record TransitionRequestDTO(String targetState, Long changedBy, String comment) {
}
