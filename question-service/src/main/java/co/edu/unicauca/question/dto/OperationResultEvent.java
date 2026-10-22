package co.edu.unicauca.question.dto;

import java.time.LocalDateTime;

/** Resultado del procesamiento de un comando, publicado por Lifecycle Service. */
public record OperationResultEvent(String operationId, Long questionId, String status, String newState,
                                   String message, LocalDateTime occurredAt) {
}
