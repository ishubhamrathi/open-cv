package com.opencv.ama.starter.provider;

import com.eclipsesource.json.Json;
import com.opencv.ama.core.exception.ProviderException;
import com.opencv.ama.core.spi.AnswerRequest;
import com.opencv.ama.core.spi.ProviderAnswer;
import com.opencv.ama.starter.config.AmaProperties;
import org.springframework.http.MediaType;

/** Anthropic Messages API provider (Claude). */
public class AnthropicProvider extends AbstractRestProvider {

    public AnthropicProvider(AmaProperties.ProviderConfig config) {
        super(config);
    }

    @Override
    protected boolean requiresKey() {
        return true;
    }

    @Override
    public ProviderAnswer answer(AnswerRequest request) {
        String body = restClient.post()
                .uri("/v1/messages")
                .header("x-api-key", config.getApiKey())
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.object()
                        .add("model", config.getModel())
                        .add("max_tokens", 1024)
                        .add("system", buildSystemPrompt(request))
                        .add("messages", Json.array()
                                .add(Json.object().add("role", "user").add("content", request.question())))
                        .toString())
                .retrieve()
                .body(String.class);
        var content = Json.parse(body).asObject().get("content").asArray();
        if (content.isEmpty()) {
            throw new ProviderException("Anthropic returned no content");
        }
        String text = content.get(0).asObject().getString("text", "").trim();
        if (text.isEmpty()) {
            throw new ProviderException("Anthropic returned an empty answer");
        }
        return new ProviderAnswer(config.getName(), text, null, config.getModel());
    }
}