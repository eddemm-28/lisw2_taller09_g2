package co.edu.unicauca.lifecycle.service;

import co.edu.unicauca.lifecycle.model.OperationResult;

/**
 * Resultado del procesamiento de un comando. duplicate=true indica que el operationId ya se
 * habia procesado (entrega repetida de RabbitMQ) y no debe volver a generar efectos.
 */
public record SubmitOutcome(OperationResult result, Long authorId, boolean duplicate) {
}
