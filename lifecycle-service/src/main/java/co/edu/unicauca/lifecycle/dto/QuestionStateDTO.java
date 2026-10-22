package co.edu.unicauca.lifecycle.dto;

import co.edu.unicauca.lifecycle.model.QuestionState;

import java.time.LocalDateTime;
import java.util.List;

public record QuestionStateDTO(Long questionId, Long authorId, String state, List<String> allowedTransitions,
                               LocalDateTime changedAt, Long changedBy, String comment) {

    public static QuestionStateDTO from(QuestionState state) {
        List<String> allowed = state.getState().allowedTransitions().stream().map(Enum::name).sorted().toList();
        return new QuestionStateDTO(state.getQuestionId(), state.getAuthorId(), state.getState().name(), allowed,
                state.getChangedAt(), state.getChangedBy(), state.getComment());
    }
}
