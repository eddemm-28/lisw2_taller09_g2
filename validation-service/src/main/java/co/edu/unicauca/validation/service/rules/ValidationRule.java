package co.edu.unicauca.validation.service.rules;

import co.edu.unicauca.validation.dto.ValidationRequestDTO;

import java.util.List;

/**
 * Regla estructural que debe cumplir una pregunta. Cada regla devuelve los errores encontrados
 * (lista vacia si la pregunta la cumple).
 */
public interface ValidationRule {

    String name();

    String description();

    default boolean active() {
        return true;
    }

    List<String> validate(ValidationRequestDTO question);
}
