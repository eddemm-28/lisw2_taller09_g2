package co.edu.unicauca.lifecycle.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Resultado del procesamiento de un comando asincrono. Permite verificar desde Postman si la
 * transicion se completo o fallo, y evita procesar dos veces el mismo operationId.
 */
@Entity
@Table(name = "operation_results")
public class OperationResult {

    @Id
    private String id;

    private Long questionId;

    private String type;

    @Enumerated(EnumType.STRING)
    private OperationStatus status;

    @Column(length = 1000)
    private String message;

    /** Estado de la pregunta despues de procesar el comando. */
    private String resultingState;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    protected OperationResult() {
    }

    public OperationResult(String id, Long questionId, String type, OperationStatus status, String message,
                           String resultingState, LocalDateTime createdAt) {
        this.id = id;
        this.questionId = questionId;
        this.type = type;
        this.status = status;
        this.message = message;
        this.resultingState = resultingState;
        this.createdAt = createdAt;
        this.completedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
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

    public String getResultingState() {
        return resultingState;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
