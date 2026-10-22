package co.edu.unicauca.question.controller;

import co.edu.unicauca.question.dto.CreateQuestionRequestDTO;
import co.edu.unicauca.question.dto.CreateQuestionResponseDTO;
import co.edu.unicauca.question.dto.QuestionDTO;
import co.edu.unicauca.question.dto.SubmitAcceptedDTO;
import co.edu.unicauca.question.service.QuestionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /** Creacion sincrona: 201 Created (DRAFT) o 400 Bad Request con los errores de validacion. */
    @PostMapping
    public ResponseEntity<CreateQuestionResponseDTO> create(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody CreateQuestionRequestDTO request) {
        CreateQuestionResponseDTO response = questionService.create(authorization, request);
        return ResponseEntity.created(URI.create("/questions/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> findAll() {
        return ResponseEntity.ok(questionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.findById(id));
    }

    /** Correccion de una pregunta rechazada para que vuelva al proceso de revision. */
    @PutMapping("/{id}")
    public ResponseEntity<QuestionDTO> correct(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id, @RequestBody CreateQuestionRequestDTO request) {
        return ResponseEntity.ok(questionService.correct(authorization, id, request));
    }

    /** Envio asincrono a revision: 202 Accepted con el operationId para consulta posterior. */
    @PostMapping("/{questionId}/submit")
    public ResponseEntity<SubmitAcceptedDTO> submitForReview(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long questionId) {
        SubmitAcceptedDTO response = questionService.submitForReview(authorization, questionId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .location(URI.create(response.statusUrl()))
                .body(response);
    }
}
