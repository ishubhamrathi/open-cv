package com.opencv.ama.starter.provider;

import com.opencv.ama.core.spi.AnswerProvider;
import com.opencv.ama.starter.config.AmaProperties;

/** Maps configured provider names to their adapter implementations. */
public final class ProviderFactory {

    private ProviderFactory() {
    }

    public static AnswerProvider create(AmaProperties.ProviderConfig config) {
        return switch (config.getName()) {
            case "openai" -> new OpenAiProvider(config);
            case "anthropic" -> new AnthropicProvider(config);
            case "gemini" -> new GeminiProvider(config);
            case "ollama" -> new OllamaProvider(config);
            default -> throw new IllegalArgumentException(
                    "Unknown provider '" + config.getName() + "'. Supported: openai, anthropic, gemini, ollama");
        };
    }
}