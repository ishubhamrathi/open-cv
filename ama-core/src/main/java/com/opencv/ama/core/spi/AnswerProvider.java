package com.opencv.ama.core.spi;

import com.opencv.ama.core.exception.ProviderException;

/**
 * An AI answer provider. Implementations wrap a specific vendor API (OpenAI, Anthropic,
 * Gemini, Ollama, ...). The engine invokes providers through a {@code ProviderChain} in
 * configured priority order, skipping unavailable providers and failing over on errors.
 */
public interface AnswerProvider {

    /** Stable name, e.g. {@code openai}. Used for priority ordering and analytics. */
    String name();

    /**
     * Whether this provider can currently serve requests (configured, key present, enabled).
     * Cheap check — called on every ask to decide failover.
     */
    boolean isAvailable();

    /**
     * Generate an answer. Must throw {@link ProviderException} on any failure so the chain can fail over.
     */
    ProviderAnswer answer(AnswerRequest request) throws ProviderException;
}
