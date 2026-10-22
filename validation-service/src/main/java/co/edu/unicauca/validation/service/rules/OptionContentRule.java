package co.edu.unicauca.validation.service.rules;

import co.edu.unicauca.validation.dto.OptionDTO;
import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@Order(3)
public class OptionContentRule implements ValidationRule {

    @Override
    public String name() {
        return "OPCIONES_COMPLETAS_Y_DISTINTAS";
    }

    @Override
    public String description() {
        return "Cada opción tiene identificador y texto, sin identificadores ni textos repetidos";
    }

    @Override
    public List<String> validate(ValidationRequestDTO question) {
        if (question.options() == null) {
            return List.of();
        }
        List<String> errors = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<String> texts = new HashSet<>();
        int position = 1;
        for (OptionDTO option : question.options()) {
            if (option == null || option.id() == null || option.id().isBlank()) {
                errors.add("La opción en la posición " + position + " no tiene identificador");
            } else if (!ids.add(option.id().trim().toUpperCase(Locale.ROOT))) {
                errors.add("El identificador de opción '" + option.id() + "' está repetido");
            }
            if (option == null || option.text() == null || option.text().isBlank()) {
                errors.add("La opción en la posición " + position + " no tiene texto");
            } else if (!texts.add(option.text().trim().toLowerCase(Locale.ROOT))) {
                errors.add("El texto de la opción '" + option.text() + "' está repetido (distractor duplicado)");
            }
            position++;
        }
        return errors;
    }
}
