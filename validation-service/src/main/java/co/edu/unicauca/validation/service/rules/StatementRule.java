package co.edu.unicauca.validation.service.rules;

import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class StatementRule implements ValidationRule {

    private final int minLength;

    public StatementRule(@Value("${validation.statement.min-length:10}") int minLength) {
        this.minLength = minLength;
    }

    @Override
    public String name() {
        return "ENUNCIADO_OBLIGATORIO";
    }

    @Override
    public String description() {
        return "El enunciado es obligatorio y debe tener al menos " + minLength + " caracteres";
    }

    @Override
    public List<String> validate(ValidationRequestDTO question) {
        String text = question.text();
        if (text == null || text.isBlank()) {
            return List.of("El enunciado de la pregunta es obligatorio");
        }
        if (text.trim().length() < minLength) {
            return List.of("El enunciado debe tener al menos " + minLength + " caracteres");
        }
        return List.of();
    }
}
