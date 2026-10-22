package co.edu.unicauca.question.dto;

/**
 * Respuesta 202 Accepted: confirma la recepcion de la solicitud, no que la transicion termino.
 */
public record SubmitAcceptedDTO(String message, String operationId, String status, String statusUrl) {
}
