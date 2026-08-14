package com.opencv.ama.starter.provider;

import com.opencv.ama.core.spi.AnswerProvider;
import com.opencv.ama.core.spi.AnswerRequest;
import com.opencv.ama.starter.config.AmaProperties;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Shared plumbing for HTTP-based AI providers: RestClient with timeouts, a default
 * "portfolio owner" system prompt and knowledge-context enrichment.
 *
 * <p>JSON is parsed with <em>minimal-json</em> (dependency-free) rather than Jackson so the
 * starter works unchanged on any Spring Boot generation.</p>
 */
public abstract class AbstractRestProvider implements AnswerProvider {

    protected static final String DEFAULT_SYSTEM_PROMPT =
            "You are the 'Ask Me Anything' assistant for Shubham's personal portfolio website "
                    + "(shubhamrathi.in). Answer questions as Shubham would: first person, friendly, "
                    + "concise and warm. Use the provided knowledge context when it matches. Never invent "
                    + "facts, credentials or links you are unsure about. If you genuinely don't know, say "
                    + "so and suggest reaching out via the contact page.";

    protected final AmaProperties.ProviderConfig config;
    protected final RestClient restClient;

    protected AbstractRestProvider(AmaProperties.ProviderConfig config) {
        this.config = config;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                java.net.http.HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build());
        factory.setReadTimeout(Duration.ofMillis(config.getTimeoutMs()));
        this.restClient = RestClient.builder()
                .baseUrl(config.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @Override
    public String name() {
        return config.getName();
    }

    @Override
    public boolean isAvailable() {
        if (!config.isEnabled()) {
            return false;
        }
        return !requiresKey() || hasKey(config.getApiKey());
    }

    /** Providers like Ollama run without an API key; override to {@code false}. */
    protected boolean requiresKey() {
        return true;
    }

    protected static boolean hasKey(String key) {
        return key != null && !key.isBlank();
    }

    protected String buildSystemPrompt(AnswerRequest request) {
        String base = (config.getSystemPrompt() != null && !config.getSystemPrompt().isBlank())
                ? config.getSystemPrompt()
                : DEFAULT_SYSTEM_PROMPT;
        if (request.knowledgeContext() == null || request.knowledgeContext().isEmpty()) {
            return base;
        }
        StringBuilder sb = new StringBuilder(base);
        sb.append("\n\nKnowledge about Shubham you can rely on (prefer it verbatim when it matches):\n");
        for (String line : request.knowledgeContext()) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }
}