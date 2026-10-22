package co.edu.unicauca.review.controller;

import co.edu.unicauca.review.dto.AssignReviewerRequestDTO;
import co.edu.unicauca.review.dto.AssignmentDTO;
import co.edu.unicauca.review.dto.ReviewDTO;
import co.edu.unicauca.review.dto.ReviewResultRequestDTO;
import co.edu.unicauca.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** Asignacion de revisor: 201 Created (evento ReviewerAssigned publicado) o 400/403/404/409/503. */
    @PostMapping("/assign")
    public ResponseEntity<AssignmentDTO> assign(@Valid @RequestBody AssignReviewerRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.assignReviewer(request));
    }

    /** Resultado de la revision (APPROVED / REJECTED con motivo). */
    @PostMapping("/result")
    public ResponseEntity<ReviewDTO> result(@Valid @RequestBody ReviewResultRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.registerResult(request));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentDTO>> assignments(@RequestParam(required = false) Long questionId) {
        return ResponseEntity.ok(reviewService.assignments(questionId));
    }

    @GetMapping("/assignments/{id}")
    public ResponseEntity<AssignmentDTO> assignment(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.assignment(id));
    }

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> reviews(@RequestParam(required = false) Long questionId) {
        return ResponseEntity.ok(reviewService.reviews(questionId));
    }
}
