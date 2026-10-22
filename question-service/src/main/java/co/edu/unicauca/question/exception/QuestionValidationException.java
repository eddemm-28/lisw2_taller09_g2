package co.edu.unicauca.question.exception;

import java.util.List;

/** La pregunta no supero la validacion estructural de Validation Service. */
public class QuestionValidationException extends RuntimeException {

    private final List<String> errors;

    public QuestionValidationException(List<String> errors) {
        super("La pregunta no superó la validación estructural y no fue registrada");
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
