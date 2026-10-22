package co.edu.unicauca.question.dto;

import co.edu.unicauca.question.model.Operation;

import java.time.LocalDateTime;

public record OperationDTO(String operationId, Long questionId, String type, String status, String message,
                           Long requestedBy, LocalDateTime createdAt, LocalDateTime completedAt) {

    public static OperationDTO from(Operation operation) {
        return new OperationDTO(operation.getOperationId(), operation.getQuestionId(), operation.getType(),
                operation.getStatus().name(), operation.getMessage(), operation.getRequestedBy(),
                operation.getCreatedAt(), operation.getCompletedAt());
    }
}
