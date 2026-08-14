package com.opencv.ama.starter.provider;

import com.eclipsesource.json.Json;
import com.opencv.ama.core.exception.ProviderException;
import com.opencv.ama.core.spi.AnswerRequest;
import com.opencv.ama.core.spi.ProviderAnswer;
import com.opencv.ama.starter.config.AmaProperties;
import org.springframework.http.MediaType;

import java.util.Map;

/** OpenAI Chat Completions provider. */
public class OpenAiProvider extends AbstractRestProvider {

    public OpenAiProvider(AmaProperties.ProviderConfig config) {
        super(config);
    }

    @Override
    protected boolean requiresKey() {
        return true;
    }

    @Override
    public ProviderAnswer answer(AnswerRequest request) {
        String body = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + config.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.object()
                        .add("model", config.getModel())
                        .add("temperature", 0.6)
                        .add("messages", Json.array()
                                .add(Json.object().add("role", "system").add("content", buildSystemPrompt(request)))
                                .add(Json.object().add("role", "user").add("content", request.question())))
                        .toString())
                .retrieve()
                .body(String.class);
        String text = Json.parse(body)
                .asObject().get("choices").asArray().get(0)
                .asObject().get("message").asObject()
                .getString("content", "").trim();
        if (text.isEmpty()) {
            throw new ProviderException("OpenAI returned an empty answer");
        }
        return new ProviderAnswer(config.getName(), text, null, config.getModel());
    }
}