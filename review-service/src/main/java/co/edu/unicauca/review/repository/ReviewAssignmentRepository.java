package co.edu.unicauca.review.repository;

import co.edu.unicauca.review.model.AssignmentStatus;
import co.edu.unicauca.review.model.ReviewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewAssignmentRepository extends JpaRepository<ReviewAssignment, Long> {

    boolean existsByQuestionIdAndStatusIn(Long questionId, Collection<AssignmentStatus> statuses);

    Optional<ReviewAssignment> findFirstByQuestionIdAndReviewerIdAndStatus(Long questionId, Long reviewerId,
                                                                          AssignmentStatus status);

    List<ReviewAssignment> findByQuestionIdOrderByIdAsc(Long questionId);
}
