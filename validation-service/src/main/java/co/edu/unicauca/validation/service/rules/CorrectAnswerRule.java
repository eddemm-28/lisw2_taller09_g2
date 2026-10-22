package co.edu.unicauca.validation.service.rules;

import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Order(4)
public class CorrectAnswerRule implements ValidationRule {

    @Override
    public String name() {
        return "CLAVE_VALIDA";
    }

    @Override
    public String description() {
        return "La respuesta correcta (clave) es obligatoria y corresponde a una de las opciones";
    }

    @Override
    public List<String> validate(ValidationRequestDTO question) {
        String key = question.correctOptionId();
        if (key == null || key.isBlank()) {
            return List.of("Debe indicar la respuesta correcta (correctOptionId)");
        }
        boolean matches = question.options() != null && question.options().stream()
                .filter(Objects::nonNull)
                .anyMatch(option -> key.trim().equalsIgnoreCase(String.valueOf(option.id()).trim()));
        if (!matches) {
            return List.of("La respuesta correcta '" + key + "' no corresponde a ninguna de las opciones");
        }
        return List.of();
    }
}
