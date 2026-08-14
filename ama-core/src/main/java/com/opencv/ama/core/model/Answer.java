package com.opencv.ama.core.model;

import java.time.Instant;
import java.util.Objects;

/** An answer attached to a question. Only visible to the asker when the question is {@code PUBLISHED}. */
public final class Answer {

    private final String id;
    private final String questionId;
    private final String content;
    private final AnswerSource source;
    private final String provider;
    private final String model;
    private final Double confidence;
    private final Instant createdAt;
    private final String approvedBy;
    private final Instant approvedAt;

    public Answer(String id, String questionId, String content, AnswerSource source, String provider,
                  String model, Double confidence, Instant createdAt, String approvedBy, Instant approvedAt) {
        this.id = Objects.requireNonNull(id);
        this.questionId = Objects.requireNonNull(questionId);
        this.content = Objects.requireNonNull(content);
        this.source = Objects.requireNonNull(source);
        this.provider = provider;
        this.model = model;
        this.confidence = confidence;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
    }

    public String id() { return id; }
    public String questionId() { return questionId; }
    public String content() { return content; }
    public AnswerSource source() { return source; }
    public String provider() { return provider; }
    public String model() { return model; }
    public Double confidence() { return confidence; }
    public Instant createdAt() { return createdAt; }
    public String approvedBy() { return approvedBy; }
    public Instant approvedAt() { return approvedAt; }

    public Answer withApproval(String approvedBy, Instant at) {
        return new Answer(id, questionId, content, source, provider, model, confidence, createdAt, approvedBy, at);
    }
}
