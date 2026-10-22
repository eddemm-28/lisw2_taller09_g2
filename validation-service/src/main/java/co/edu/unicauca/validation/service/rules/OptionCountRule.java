package co.edu.unicauca.validation.service.rules;

import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
public class OptionCountRule implements ValidationRule {

    static final int REQUIRED_OPTIONS = 4;

    @Override
    public String name() {
        return "CUATRO_OPCIONES";
    }

    @Override
    public String description() {
        return "La pregunta debe tener exactamente " + REQUIRED_OPTIONS + " opciones de respuesta";
    }

    @Override
    public List<String> validate(ValidationRequestDTO question) {
        int count = question.options() == null ? 0 : question.options().size();
        if (count != REQUIRED_OPTIONS) {
            return List.of("La pregunta debe tener exactamente " + REQUIRED_OPTIONS + " opciones (tiene " + count + ")");
        }
        return List.of();
    }
}
