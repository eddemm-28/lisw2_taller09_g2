package co.edu.unicauca.validation.service;

import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import co.edu.unicauca.validation.dto.ValidationResultDTO;
import co.edu.unicauca.validation.dto.ValidationRuleDTO;
import co.edu.unicauca.validation.service.rules.ValidationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidationService {

    private static final Logger log = LoggerFactory.getLogger(ValidationService.class);

    private final List<ValidationRule> rules;

    public ValidationService(List<ValidationRule> rules) {
        this.rules = rules;
    }

    public ValidationResultDTO validate(ValidationRequestDTO question) {
        List<String> errors = rules.stream()
                .filter(ValidationRule::active)
                .flatMap(rule -> rule.validate(question).stream())
                .toList();
        log.info("[ValidationService] Pregunta validada: valid={} errores={}", errors.isEmpty(), errors.size());
        return new ValidationResultDTO(errors.isEmpty(), errors);
    }

    public List<ValidationRuleDTO> listRules() {
        return rules.stream()
                .map(rule -> new ValidationRuleDTO(rule.name(), rule.description(), rule.active()))
                .toList();
    }
}
