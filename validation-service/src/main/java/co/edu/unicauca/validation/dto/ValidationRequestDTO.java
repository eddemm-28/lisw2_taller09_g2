package co.edu.unicauca.validation.dto;

import java.util.List;

public record ValidationRequestDTO(String text, List<OptionDTO> options, String correctOptionId) {
}
