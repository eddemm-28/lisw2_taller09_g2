package co.edu.unicauca.lifecycle.controller;

import co.edu.unicauca.lifecycle.dto.OperationResultDTO;
import co.edu.unicauca.lifecycle.dto.QuestionStateDTO;
import co.edu.unicauca.lifecycle.dto.RegisterQuestionRequestDTO;
import co.edu.unicauca.lifecycle.dto.StateTransitionDTO;
import co.edu.unicauca.lifecycle.dto.TransitionRequestDTO;
import co.edu.unicauca.lifecycle.service.LifecycleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {

    private final LifecycleService lifecycleService;

    public LifecycleController(LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @PostMapping("/questions")
    public ResponseEntity<QuestionStateDTO> register(@Valid @RequestBody RegisterQuestionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lifecycleService.register(request));
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionStateDTO> getState(@PathVariable Long questionId) {
        return ResponseEntity.ok(lifecycleService.getState(questionId));
    }

    /** Transicion sincrona: 200 OK, 409 Conflict si no esta permitida, 400 si falta el motivo. */
    @PostMapping("/{questionId}/transition")
    public ResponseEntity<QuestionStateDTO> transition(@PathVariable Long questionId,
                                                       @Valid @RequestBody TransitionRequestDTO request) {
        return ResponseEntity.ok(lifecycleService.transition(questionId, request));
    }

    @GetMapping("/{questionId}/history")
    public ResponseEntity<List<StateTransitionDTO>> history(@PathVariable Long questionId) {
        return ResponseEntity.ok(lifecycleService.history(questionId));
    }

    /** Resultado registrado del procesamiento de un comando asincrono. */
    @GetMapping("/operations/{operationId}")
    public ResponseEntity<OperationResultDTO> getOperation(@PathVariable String operationId) {
        return ResponseEntity.ok(lifecycleService.getOperation(operationId));
    }
}
