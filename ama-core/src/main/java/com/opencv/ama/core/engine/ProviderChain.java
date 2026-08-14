package com.opencv.ama.core.engine;

import com.opencv.ama.core.exception.ProviderUnavailableException;
import com.opencv.ama.core.spi.AnswerProvider;
import com.opencv.ama.core.spi.AnswerRequest;
import com.opencv.ama.core.spi.ProviderAnswer;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs providers in configured order and fails over to the next one on any error.
 * If every configured provider is unavailable or throws, it raises
 * {@link ProviderUnavailableException} with per-provider failure details.
 */
public final class ProviderChain {

    private final List<AnswerProvider> providers;

    public ProviderChain(List<AnswerProvider> providers, List<String> priorityOrder) {
        List<AnswerProvider> ordered = new ArrayList<>(providers);
        if (priorityOrder != null && !priorityOrder.isEmpty()) {
            ordered.sort((a, b) -> {
                int ia = rank(priorityOrder, a.name());
                int ib = rank(priorityOrder, b.name());
                return Integer.compare(ia, ib);
            });
        }
        this.providers = List.copyOf(ordered);
    }

    private static int rank(List<String> order, String name) {
        int i = order.indexOf(name);
        return i < 0 ? Integer.MAX_VALUE : i;
    }

    /** Names of providers in the effective chain order. */
    public List<String> order() {
        return providers.stream().map(AnswerProvider::name).toList();
    }

    /** Providers in the effective chain order (immutable). */
    public List<AnswerProvider> providers() {
        return providers;
    }

    public ProviderAnswer answer(AnswerRequest request) {
        List<String> failures = new ArrayList<>();
        for (AnswerProvider provider : providers) {
            if (!provider.isAvailable()) {
                failures.add(provider.name() + ": unavailable");
                continue;
            }
            try {
                return provider.answer(request);
            } catch (RuntimeException e) {
                failures.add(provider.name() + ": " + e.getMessage());
            }
        }
        throw new ProviderUnavailableException(
                "No answer provider could produce a response: " + String.join("; ", failures));
    }
}