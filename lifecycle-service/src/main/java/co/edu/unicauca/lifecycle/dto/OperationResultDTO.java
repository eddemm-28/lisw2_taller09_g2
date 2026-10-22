package co.edu.unicauca.lifecycle.dto;

import co.edu.unicauca.lifecycle.model.OperationResult;

import java.time.LocalDateTime;

public record OperationResultDTO(String operationId, Long questionId, String type, String status, String message,
                                 String resultingState, LocalDateTime createdAt, LocalDateTime completedAt) {

    public static OperationResultDTO from(OperationResult result) {
        return new OperationResultDTO(result.getId(), result.getQuestionId(), result.getType(),
                result.getStatus().name(), result.getMessage(), result.getResultingState(),
                result.getCreatedAt(), result.getCompletedAt());
    }
}
