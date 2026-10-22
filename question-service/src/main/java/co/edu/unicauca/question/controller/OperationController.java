package co.edu.unicauca.question.controller;

import co.edu.unicauca.question.dto.OperationDTO;
import co.edu.unicauca.question.service.OperationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operations")
public class OperationController {

    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    /** Consulta del resultado de una operacion asincrona: PENDING, COMPLETED o FAILED. */
    @GetMapping("/{operationId}")
    public ResponseEntity<OperationDTO> findById(@PathVariable String operationId) {
        return ResponseEntity.ok(operationService.findById(operationId));
    }
}
