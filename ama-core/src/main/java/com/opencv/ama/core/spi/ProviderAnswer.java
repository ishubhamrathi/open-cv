package com.opencv.ama.core.spi;

/** Successful result from an {@link AnswerProvider}. */
public record ProviderAnswer(
        String providerName,
        String content,
        Double confidence,
        String model
) {
    public ProviderAnswer {
        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException("Provider name is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Provider returned an empty answer");
        }
    }
}