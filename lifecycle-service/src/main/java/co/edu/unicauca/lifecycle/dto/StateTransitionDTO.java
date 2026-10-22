package co.edu.unicauca.lifecycle.dto;

import co.edu.unicauca.lifecycle.model.StateTransition;

import java.time.LocalDateTime;

public record StateTransitionDTO(String fromState, String toState, Long changedBy, String comment,
                                 String operationId, LocalDateTime changedAt) {

    public static StateTransitionDTO from(StateTransition transition) {
        String from = transition.getFromState() == null ? null : transition.getFromState().name();
        return new StateTransitionDTO(from, transition.getToState().name(), transition.getChangedBy(),
                transition.getComment(), transition.getOperationId(), transition.getChangedAt());
    }
}
