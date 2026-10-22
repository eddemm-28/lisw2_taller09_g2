package co.edu.unicauca.lifecycle.dto;

import java.time.LocalDateTime;

public record OperationResultEvent(String operationId, Long questionId, String status, String newState,
                                   String message, LocalDateTime occurredAt) {
}
