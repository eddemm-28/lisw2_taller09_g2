package co.edu.unicauca.question.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Option {

    @Column(name = "option_id", nullable = false)
    private String optionId;

    @Column(name = "option_text", nullable = false, length = 1000)
    private String text;

    protected Option() {
    }

    public Option(String optionId, String text) {
        this.optionId = optionId;
        this.text = text;
    }

    public String getOptionId() {
        return optionId;
    }

    public String getText() {
        return text;
    }
}
