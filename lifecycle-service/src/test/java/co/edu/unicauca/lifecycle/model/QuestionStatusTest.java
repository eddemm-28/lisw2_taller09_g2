package co.edu.unicauca.lifecycle.model;

import co.edu.unicauca.lifecycle.exception.InvalidTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionStatusTest {

    @Test
    void reviewFlowTransitionsAreAllowed() {
        assertThat(QuestionStatus.DRAFT.canTransitionTo(QuestionStatus.PENDING_REVIEW)).isTrue();
        assertThat(QuestionStatus.PENDING_REVIEW.canTransitionTo(QuestionStatus.IN_REVIEW)).isTrue();
        assertThat(QuestionStatus.IN_REVIEW.canTransitionTo(QuestionStatus.APPROVED)).isTrue();
        assertThat(QuestionStatus.IN_REVIEW.canTransitionTo(QuestionStatus.REJECTED)).isTrue();
        assertThat(QuestionStatus.REJECTED.canTransitionTo(QuestionStatus.DRAFT)).isTrue();
    }

    @Test
    void draftCannotBeApproved() {
        assertThat(QuestionStatus.DRAFT.canTransitionTo(QuestionStatus.APPROVED)).isFalse();
    }

    @Test
    void pendingReviewCannotBeSubmittedAgain() {
        assertThat(QuestionStatus.PENDING_REVIEW.canTransitionTo(QuestionStatus.PENDING_REVIEW)).isFalse();
    }

    @Test
    void invalidTransitionKeepsCurrentState() {
        QuestionState state = new QuestionState(1L, 10L);

        assertThatThrownBy(() -> state.applyTransition(QuestionStatus.APPROVED, 4L, "aprobar"))
                .isInstanceOf(InvalidTransitionException.class)
                .hasMessageContaining("DRAFT -> APPROVED");
        assertThat(state.getState()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void validTransitionRecordsAuthorAndComment() {
        QuestionState state = new QuestionState(1L, 10L);

        QuestionStatus previous = state.applyTransition(QuestionStatus.PENDING_REVIEW, 10L, "enviada");

        assertThat(previous).isEqualTo(QuestionStatus.DRAFT);
        assertThat(state.getState()).isEqualTo(QuestionStatus.PENDING_REVIEW);
        assertThat(state.getChangedBy()).isEqualTo(10L);
        assertThat(state.getComment()).isEqualTo("enviada");
    }
}
