package co.edu.unicauca.question.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Contenido de la pregunta. El estado NO se guarda aqui: su unica autoridad es Lifecycle Service.
 */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String text;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "position")
    private List<Option> options = new ArrayList<>();

    @Column(nullable = false)
    private String correctOptionId;

    @Column(nullable = false)
    private Long authorId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected Question() {
    }

    public Question(String text, List<Option> options, String correctOptionId, Long authorId) {
        this.text = text;
        this.options = new ArrayList<>(options);
        this.correctOptionId = correctOptionId;
        this.authorId = authorId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    public void updateContent(String text, List<Option> options, String correctOptionId) {
        this.text = text;
        this.options.clear();
        this.options.addAll(options);
        this.correctOptionId = correctOptionId;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAuthoredBy(Long userId) {
        return authorId.equals(userId);
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<Option> getOptions() {
        return options;
    }

    public String getCorrectOptionId() {
        return correctOptionId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
