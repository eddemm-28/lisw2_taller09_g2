package co.edu.unicauca.question.dto;

import java.util.List;

public record ValidationRequestDTO(String text, List<OptionDTO> options, String correctOptionId) {
}
