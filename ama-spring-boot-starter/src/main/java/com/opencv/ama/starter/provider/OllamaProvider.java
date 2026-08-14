package com.opencv.ama.starter.provider;

import com.eclipsesource.json.Json;
import com.opencv.ama.core.exception.ProviderException;
import com.opencv.ama.core.spi.AnswerRequest;
import com.opencv.ama.core.spi.ProviderAnswer;
import com.opencv.ama.starter.config.AmaProperties;
import org.springframework.http.MediaType;

/** Ollama local chat provider (no API key required). */
public class OllamaProvider extends AbstractRestProvider {

    public OllamaProvider(AmaProperties.ProviderConfig config) {
        super(config);
    }

    @Override
    protected boolean requiresKey() {
        return false;
    }

    @Override
    public ProviderAnswer answer(AnswerRequest request) {
        String body = restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.object()
                        .add("model", config.getModel())
                        .add("stream", false)
                        .add("messages", Json.array()
                                .add(Json.object().add("role", "system").add("content", buildSystemPrompt(request)))
                                .add(Json.object().add("role", "user").add("content", request.question())))
                        .toString())
                .retrieve()
                .body(String.class);
        String text = Json.parse(body)
                .asObject().get("message").asObject()
                .getString("content", "").trim();
        if (text.isEmpty()) {
            throw new ProviderException("Ollama returned an empty answer");
        }
        return new ProviderAnswer(config.getName(), text, null, config.getModel());
    }
}