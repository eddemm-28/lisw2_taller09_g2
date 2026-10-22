package co.edu.unicauca.lifecycle.model;

import co.edu.unicauca.lifecycle.exception.InvalidTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Estado actual de una pregunta. Solo Lifecycle Service lo modifica. */
@Entity
@Table(name = "question_states")
public class QuestionState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long questionId;

    private Long authorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionStatus state;

    private LocalDateTime changedAt;

    private Long changedBy;

    @Column(length = 1000)
    private String comment;

    protected QuestionState() {
    }

    public QuestionState(Long questionId, Long authorId) {
        this.questionId = questionId;
        this.authorId = authorId;
        this.state = QuestionStatus.DRAFT;
        this.changedAt = LocalDateTime.now();
        this.changedBy = authorId;
        this.comment = "Pregunta creada y validada";
    }

    public boolean canTransitionTo(QuestionStatus newState) {
        return state.canTransitionTo(newState);
    }

    public QuestionStatus applyTransition(QuestionStatus newState, Long changedBy, String comment) {
        if (!canTransitionTo(newState)) {
            throw new InvalidTransitionException("Transición no permitida: " + state + " -> " + newState
                    + ". Desde " + state + " solo se permite " + state.allowedTransitions());
        }
        QuestionStatus previous = state;
        this.state = newState;
        this.changedBy = changedBy;
        this.comment = comment;
        this.changedAt = LocalDateTime.now();
        return previous;
    }

    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public QuestionStatus getState() {
        return state;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public String getComment() {
        return comment;
    }
}
