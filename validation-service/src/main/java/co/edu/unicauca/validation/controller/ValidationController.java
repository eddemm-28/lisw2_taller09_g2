package co.edu.unicauca.validation.controller;

import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import co.edu.unicauca.validation.dto.ValidationResultDTO;
import co.edu.unicauca.validation.dto.ValidationRuleDTO;
import co.edu.unicauca.validation.service.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/validate")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    /**
     * Siempre responde 200 con el resultado (valid + errores); es Question Service quien decide
     * responder 400 al cliente cuando la pregunta no es valida.
     */
    @PostMapping
    public ResponseEntity<ValidationResultDTO> validate(@RequestBody ValidationRequestDTO request) {
        return ResponseEntity.ok(validationService.validate(request));
    }

    @GetMapping("/rules")
    public ResponseEntity<List<ValidationRuleDTO>> rules() {
        return ResponseEntity.ok(validationService.listRules());
    }
}
