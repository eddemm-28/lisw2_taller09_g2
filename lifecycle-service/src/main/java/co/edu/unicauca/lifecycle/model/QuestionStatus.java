package co.edu.unicauca.lifecycle.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Estados de la pregunta y transiciones permitidas (maquina de estados del ciclo de vida).
 */
public enum QuestionStatus {
    DRAFT,
    PENDING_REVIEW,
    IN_REVIEW,
    APPROVED,
    REJECTED,
    PUBLISHED,
    ARCHIVED;

    private static final Map<QuestionStatus, Set<QuestionStatus>> TRANSITIONS = new EnumMap<>(QuestionStatus.class);

    static {
        TRANSITIONS.put(DRAFT, EnumSet.of(PENDING_REVIEW));
        TRANSITIONS.put(PENDING_REVIEW, EnumSet.of(IN_REVIEW));
        TRANSITIONS.put(IN_REVIEW, EnumSet.of(APPROVED, REJECTED));
        TRANSITIONS.put(REJECTED, EnumSet.of(DRAFT));
        TRANSITIONS.put(APPROVED, EnumSet.of(PUBLISHED));
        TRANSITIONS.put(PUBLISHED, EnumSet.of(ARCHIVED));
        TRANSITIONS.put(ARCHIVED, EnumSet.noneOf(QuestionStatus.class));
    }

    public boolean canTransitionTo(QuestionStatus target) {
        return TRANSITIONS.get(this).contains(target);
    }

    public Set<QuestionStatus> allowedTransitions() {
        return Collections.unmodifiableSet(TRANSITIONS.get(this));
    }
}
