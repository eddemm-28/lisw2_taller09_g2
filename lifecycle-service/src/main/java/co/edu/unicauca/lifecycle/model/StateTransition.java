package co.edu.unicauca.lifecycle.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Historial (auditoria) de cada transicion aplicada, incluido el motivo. */
@Entity
@Table(name = "state_transitions")
public class StateTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long questionId;

    @Enumerated(EnumType.STRING)
    private QuestionStatus fromState;

    @Enumerated(EnumType.STRING)
    private QuestionStatus toState;

    private Long changedBy;

    @Column(length = 1000)
    private String comment;

    private String operationId;

    private LocalDateTime changedAt;

    protected StateTransition() {
    }

    public StateTransition(Long questionId, QuestionStatus fromState, QuestionStatus toState, Long changedBy,
                           String comment, String operationId) {
        this.questionId = questionId;
        this.fromState = fromState;
        this.toState = toState;
        this.changedBy = changedBy;
        this.comment = comment;
        this.operationId = operationId;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public QuestionStatus getFromState() {
        return fromState;
    }

    public QuestionStatus getToState() {
        return toState;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public String getComment() {
        return comment;
    }

    public String getOperationId() {
        return operationId;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
