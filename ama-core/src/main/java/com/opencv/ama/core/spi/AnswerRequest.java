package com.opencv.ama.core.spi;

import java.util.List;

/** Request handed to an {@link AnswerProvider}. */
public record AnswerRequest(
        String question,
        String askerName,
        List<String> knowledgeContext
) {
    public AnswerRequest {
        knowledgeContext = knowledgeContext == null ? List.of() : List.copyOf(knowledgeContext);
    }
}
