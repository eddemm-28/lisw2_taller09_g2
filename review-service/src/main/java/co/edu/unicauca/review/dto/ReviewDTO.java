package co.edu.unicauca.review.dto;

import co.edu.unicauca.review.model.Review;

import java.time.LocalDateTime;

public record ReviewDTO(Long id, Long assignmentId, Long questionId, Long reviewerId, String result, String comments,
                        String questionState, LocalDateTime createdAt) {

    public static ReviewDTO from(Review review, String questionState) {
        return new ReviewDTO(review.getId(), review.getAssignmentId(), review.getQuestionId(), review.getReviewerId(),
                review.getResult().name(), review.getComments(), questionState, review.getCreatedAt());
    }
}
