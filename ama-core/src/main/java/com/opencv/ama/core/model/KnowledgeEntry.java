package com.opencv.ama.core.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A curated question/answer pair used by the {@code KNOWLEDGE_FIRST} mode and to give AI providers
 * a voice/context. Think of it as the FAQ entries the owner curates.
 */
public final class KnowledgeEntry {

    private final String id;
    private final String category;
    private final String question;
    private final String answer;
    private final List<String> keywords;
    private final Double confidence;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;

    public KnowledgeEntry(String id, String category, String question, String answer,
                          List<String> keywords, Double confidence, boolean active,
                          Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.category = category;
        this.question = Objects.requireNonNull(question);
        this.answer = Objects.requireNonNull(answer);
        this.keywords = keywords == null ? List.of() : List.copyOf(keywords);
        this.confidence = confidence;
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public String id() { return id; }
    public String category() { return category; }
    public String question() { return question; }
    public String answer() { return answer; }
    public List<String> keywords() { return keywords; }
    public Double confidence() { return confidence; }
    public boolean active() { return active; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
