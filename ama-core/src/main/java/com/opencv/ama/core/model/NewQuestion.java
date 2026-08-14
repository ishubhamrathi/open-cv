package com.opencv.ama.core.model;

import java.util.Objects;

/** Input for creating a new Ask Me Anything question. */
public record NewQuestion(
        String id,
        String reference,
        String askerName,
        String askerEmail,
        String question,
        String category,
        WorkflowMode mode
) {
    public NewQuestion {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(question, "question");
        Objects.requireNonNull(mode, "mode");
    }
}
