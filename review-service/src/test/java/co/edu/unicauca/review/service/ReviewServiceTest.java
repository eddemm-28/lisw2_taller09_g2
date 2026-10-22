package co.edu.unicauca.review.service;

import co.edu.unicauca.review.client.LifecycleClient;
import co.edu.unicauca.review.client.UserClient;
import co.edu.unicauca.review.dto.AssignReviewerRequestDTO;
import co.edu.unicauca.review.dto.AssignmentDTO;
import co.edu.unicauca.review.dto.QuestionStateDTO;
import co.edu.unicauca.review.dto.UserDTO;
import co.edu.unicauca.review.exception.ConflictException;
import co.edu.unicauca.review.exception.ServiceUnavailableException;
import co.edu.unicauca.review.messaging.EventPublisher;
import co.edu.unicauca.review.model.AssignmentStatus;
import co.edu.unicauca.review.model.ReviewAssignment;
import co.edu.unicauca.review.repository.ReviewAssignmentRepository;
import co.edu.unicauca.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

    private final ReviewAssignmentRepository assignmentRepository = mock(ReviewAssignmentRepository.class);
    private final UserClient userClient = mock(UserClient.class);
    private final LifecycleClient lifecycleClient = mock(LifecycleClient.class);
    private final EventPublisher eventPublisher = mock(EventPublisher.class);
    private final ReviewService service = new ReviewService(assignmentRepository, mock(ReviewRepository.class),
            userClient, lifecycleClient, eventPublisher);

    @BeforeEach
    void setUp() {
        when(userClient.getUser(4L)).thenReturn(new UserDTO(4L, "admin", "Admin", "a@u", true, Set.of("ADMIN")));
        when(userClient.getUser(2L)).thenReturn(new UserDTO(2L, "revisor1", "Rev", "r@u", true, Set.of("REVIEWER")));
        when(lifecycleClient.getState(1L)).thenReturn(
                new QuestionStateDTO(1L, 1L, "PENDING_REVIEW", List.of("IN_REVIEW"), null, 1L, null));
        when(assignmentRepository.save(any(ReviewAssignment.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void confirmedAssignmentPublishesReviewerAssigned() {
        when(eventPublisher.publishReviewerAssigned(any())).thenReturn(true);

        AssignmentDTO result = service.assignReviewer(new AssignReviewerRequestDTO(1L, 2L, 4L));

        assertThat(result.status()).isEqualTo("ASSIGNED");
        assertThat(result.notificationPublished()).isTrue();
        verify(lifecycleClient).requestTransition(eq(1L), eq("IN_REVIEW"), eq(4L), anyString());
    }

    @Test
    void assignmentIsCompensatedWhenLifecycleIsUnavailable() {
        when(lifecycleClient.requestTransition(anyLong(), anyString(), anyLong(), anyString()))
                .thenThrow(new ServiceUnavailableException("Lifecycle Service no disponible"));

        assertThatThrownBy(() -> service.assignReviewer(new AssignReviewerRequestDTO(1L, 2L, 4L)))
                .isInstanceOf(ServiceUnavailableException.class);

        ArgumentCaptor<ReviewAssignment> saved = ArgumentCaptor.forClass(ReviewAssignment.class);
        verify(assignmentRepository, atLeastOnce()).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(AssignmentStatus.CANCELLED);
        verify(eventPublisher, never()).publishReviewerAssigned(any());
    }

    @Test
    void questionInDraftIsNotEligible() {
        when(lifecycleClient.getState(1L)).thenReturn(
                new QuestionStateDTO(1L, 1L, "DRAFT", List.of("PENDING_REVIEW"), null, 1L, null));

        assertThatThrownBy(() -> service.assignReviewer(new AssignReviewerRequestDTO(1L, 2L, 4L)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("PENDING_REVIEW");
        verify(assignmentRepository, never()).save(any());
    }
}
