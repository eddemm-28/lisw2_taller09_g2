package co.edu.unicauca.validation.service;

import co.edu.unicauca.validation.dto.OptionDTO;
import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import co.edu.unicauca.validation.dto.ValidationResultDTO;
import co.edu.unicauca.validation.service.rules.CorrectAnswerRule;
import co.edu.unicauca.validation.service.rules.OptionContentRule;
import co.edu.unicauca.validation.service.rules.OptionCountRule;
import co.edu.unicauca.validation.service.rules.StatementRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationServiceTest {

    private final ValidationService service = new ValidationService(List.of(
            new StatementRule(10), new OptionCountRule(), new OptionContentRule(), new CorrectAnswerRule()));

    private static List<OptionDTO> fourOptions() {
        return List.of(new OptionDTO("A", "Singleton"), new OptionDTO("B", "Observer"),
                new OptionDTO("C", "Adapter"), new OptionDTO("D", "Decorator"));
    }

    @Test
    void validQuestionHasNoErrors() {
        ValidationResultDTO result = service.validate(new ValidationRequestDTO(
                "¿Qué patrón garantiza una única instancia de una clase?", fourOptions(), "A"));

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void questionWithThreeOptionsIsRejected() {
        ValidationResultDTO result = service.validate(new ValidationRequestDTO(
                "¿Qué patrón garantiza una única instancia?", fourOptions().subList(0, 3), "A"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("exactamente 4 opciones"));
    }

    @Test
    void correctAnswerMustBeOneOfTheOptions() {
        ValidationResultDTO result = service.validate(new ValidationRequestDTO(
                "¿Qué patrón garantiza una única instancia?", fourOptions(), "E"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("'E'"));
    }

    @Test
    void duplicatedDistractorsAndBlankStatementAreReported() {
        List<OptionDTO> options = List.of(new OptionDTO("A", "Observer"), new OptionDTO("B", "observer"),
                new OptionDTO("C", "Adapter"), new OptionDTO("D", ""));

        ValidationResultDTO result = service.validate(new ValidationRequestDTO("  ", options, null));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).hasSize(4)
                .anyMatch(error -> error.contains("enunciado"))
                .anyMatch(error -> error.contains("repetido"))
                .anyMatch(error -> error.contains("no tiene texto"))
                .anyMatch(error -> error.contains("respuesta correcta"));
    }
}
