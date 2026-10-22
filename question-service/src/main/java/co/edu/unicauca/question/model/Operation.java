package co.edu.unicauca.question.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Seguimiento local de una operacion asincrona. Nace en PENDING al responder 202 Accepted y solo
 * cambia a COMPLETED o FAILED cuando llega el resultado real desde Lifecycle Service.
 */
@Entity
@Table(name = "operations")
public class Operation {

    @Id
    private String operationId;

    private Long questionId;

    private String type;

    @Enumerated(EnumType.STRING)
    private OperationStatus status;

    @Column(length = 1000)
    private String message;

    private Long requestedBy;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    protected Operation() {
    }

    public Operation(String operationId, Long questionId, String type, Long requestedBy) {
        this.operationId = operationId;
        this.questionId = questionId;
        this.type = type;
        this.requestedBy = requestedBy;
        this.status = OperationStatus.PENDING;
        this.message = "Solicitud aceptada; en espera del procesamiento en Lifecycle Service";
        this.createdAt = LocalDateTime.now();
    }

    public void complete(String message) {
        finish(OperationStatus.COMPLETED, message);
    }

    public void fail(String message) {
        finish(OperationStatus.FAILED, message);
    }

    private void finish(OperationStatus status, String message) {
        this.status = status;
        this.message = message;
        this.completedAt = LocalDateTime.now();
    }

    public String getOperationId() {
        return operationId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getType() {
        return type;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Long getRequestedBy() {
        return requestedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
