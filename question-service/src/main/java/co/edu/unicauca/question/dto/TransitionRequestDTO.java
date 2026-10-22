package co.edu.unicauca.question.dto;

public record TransitionRequestDTO(String targetState, Long changedBy, String comment) {
}
