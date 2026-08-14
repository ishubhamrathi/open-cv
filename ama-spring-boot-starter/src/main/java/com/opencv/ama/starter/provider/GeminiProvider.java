package com.opencv.ama.starter.provider;

import com.eclipsesource.json.Json;
import com.opencv.ama.core.exception.ProviderException;
import com.opencv.ama.core.spi.AnswerRequest;
import com.opencv.ama.core.spi.ProviderAnswer;
import com.opencv.ama.starter.config.AmaProperties;
import org.springframework.http.MediaType;

/** Google Gemini generateContent provider. */
public class GeminiProvider extends AbstractRestProvider {

    public GeminiProvider(AmaProperties.ProviderConfig config) {
        super(config);
    }

    @Override
    protected boolean requiresKey() {
        return true;
    }

    @Override
    public ProviderAnswer answer(AnswerRequest request) {
        String body = restClient.post()
                .uri("/v1beta/models/" + config.getModel() + ":generateContent?key=" + config.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.object()
                        .add("systemInstruction", Json.object()
                                .add("parts", Json.array()
                                        .add(Json.object().add("text", buildSystemPrompt(request)))))
                        .add("contents", Json.array()
                                .add(Json.object().add("role", "user")
                                        .add("parts", Json.array()
                                                .add(Json.object().add("text", request.question())))))
                        .toString())
                .retrieve()
                .body(String.class);
        var candidates = Json.parse(body).asObject().get("candidates").asArray();
        if (candidates.isEmpty()) {
            throw new ProviderException("Gemini returned no candidates");
        }
        var parts = candidates.get(0).asObject().get("content").asObject().get("parts").asArray();
        if (parts.isEmpty()) {
            throw new ProviderException("Gemini returned no parts");
        }
        String text = parts.get(0).asObject().getString("text", "").trim();
        if (text.isEmpty()) {
            throw new ProviderException("Gemini returned an empty answer");
        }
        return new ProviderAnswer(config.getName(), text, null, config.getModel());
    }
}