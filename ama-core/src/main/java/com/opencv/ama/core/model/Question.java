package com.opencv.ama.core.model;

import java.time.Instant;
import java.util.Objects;

/** An Ask Me Anything question as seen by the engine and the store. */
public final class Question {

    private final String id;
    private final String reference;
    private final String askerName;
    private final String askerEmail;
    private final String question;
    private final String category;
    private final WorkflowMode mode;
    private final Instant createdAt;
    private QuestionStatus status;
    private Instant answeredAt;

    public Question(String id, String reference, String askerName, String askerEmail, String question,
                    String category, WorkflowMode mode, QuestionStatus status,
                    Instant createdAt, Instant answeredAt) {
        this.id = Objects.requireNonNull(id);
        this.reference = Objects.requireNonNull(reference);
        this.askerName = askerName;
        this.askerEmail = askerEmail;
        this.question = Objects.requireNonNull(question);
        this.category = category;
        this.mode = Objects.requireNonNull(mode);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.answeredAt = answeredAt;
    }

    public String id() { return id; }
    public String reference() { return reference; }
    public String askerName() { return askerName; }
    public String askerEmail() { return askerEmail; }
    public String question() { return question; }
    public String category() { return category; }
    public WorkflowMode mode() { return mode; }
    public QuestionStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant answeredAt() { return answeredAt; }

    public Question withStatus(QuestionStatus newStatus, Instant at) {
        this.status = newStatus;
        if (newStatus == QuestionStatus.PUBLISHED) {
            this.answeredAt = at;
        }
        return this;
    }

    public Question withAnsweredAt(Instant at) {
        this.answeredAt = at;
        return this;
    }
}
