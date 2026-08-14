package com.opencv.ama.starter.config;

import com.opencv.ama.core.model.WorkflowMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration surface for the Ask Me Anything starter.
 *
 * <pre>
 * ama:
 *   default-mode: AUTO            # AUTO | REVIEW | MANUAL | KNOWLEDGE_FIRST
 *   ai-mode: AUTO                 # fallback used by KNOWLEDGE_FIRST
 *   knowledge-threshold: 0.5
 *   rate-limit:
 *     enabled: true
 *     max-per-hour: 20
 *   jdbc:
 *     ddl-enabled: true
 *     table-prefix: ama_
 *   admin:
 *     base-path: /api/ama/admin
 *   providers:
 *     openai:    { enabled: false, api-key: ${AMA_OPENAI_API_KEY:},   model: gpt-4o-mini,  base-url: https://api.openai.com/v1 }
 *     anthropic: { enabled: false, api-key: ${AMA_ANTHROPIC_API_KEY:}, model: claude-3-5-haiku-latest, base-url: https://api.anthropic.com/v1 }
 *     gemini:    { enabled: false, api-key: ${AMA_GEMINI_API_KEY:},   model: gemini-1.5-flash, base-url: https://generativelanguage.googleapis.com/v1beta }
 *     ollama:    { enabled: false, api-key: ,                          model: llama3.2,    base-url: http://localhost:11434 }
 * </pre>
 *
 * <p>Providers are auto-registered only when {@code enabled: true} (and, where relevant,
 * a key is present). They are tried in chain order with automatic failover.</p>
 */
@ConfigurationProperties(prefix = "ama")
public class AmaProperties {

    private WorkflowMode defaultMode = WorkflowMode.AUTO;
    private WorkflowMode aiMode = WorkflowMode.AUTO;
    private double knowledgeThreshold = 0.5;
    private int maxQuestionLength = 1000;
    private int maxAskerNameLength = 120;
    /** Optional provider priority order (e.g. {@code openai,anthropic,ollama}). */
    private java.util.List<String> providerOrder = java.util.List.of();

    private RateLimit rateLimit = new RateLimit();
    private Admin admin = new Admin();
    private Jdbc jdbc = new Jdbc();
    private Providers providers = new Providers();

    public WorkflowMode getDefaultMode() { return defaultMode; }
    public void setDefaultMode(WorkflowMode defaultMode) { this.defaultMode = defaultMode; }

    public WorkflowMode getAiMode() { return aiMode; }
    public void setAiMode(WorkflowMode aiMode) { this.aiMode = aiMode; }

    public double getKnowledgeThreshold() { return knowledgeThreshold; }
    public void setKnowledgeThreshold(double knowledgeThreshold) { this.knowledgeThreshold = knowledgeThreshold; }

    public int getMaxQuestionLength() { return maxQuestionLength; }
    public void setMaxQuestionLength(int maxQuestionLength) { this.maxQuestionLength = maxQuestionLength; }

    public int getMaxAskerNameLength() { return maxAskerNameLength; }
    public void setMaxAskerNameLength(int maxAskerNameLength) { this.maxAskerNameLength = maxAskerNameLength; }

    public java.util.List<String> getProviderOrder() { return providerOrder; }
    public void setProviderOrder(java.util.List<String> providerOrder) { this.providerOrder = providerOrder == null ? java.util.List.of() : providerOrder; }

    public RateLimit getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }

    public Admin getAdmin() { return admin; }
    public void setAdmin(Admin admin) { this.admin = admin; }

    public Jdbc getJdbc() { return jdbc; }
    public void setJdbc(Jdbc jdbc) { this.jdbc = jdbc; }

    public Providers getProviders() { return providers; }
    public void setProviders(Providers providers) { this.providers = providers; }

    /** Ordered provider list derived from enabled providers. */
    public java.util.List<ProviderConfig> enabledProviders() {
        java.util.List<ProviderConfig> result = new java.util.ArrayList<>();
        if (providers.openai.enabled) result.add(providers.openai);
        if (providers.anthropic.enabled) result.add(providers.anthropic);
        if (providers.gemini.enabled) result.add(providers.gemini);
        if (providers.ollama.enabled) result.add(providers.ollama);
        return result;
    }

    public static class RateLimit {
        private boolean enabled = true;
        private int maxPerHour = 20;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMaxPerHour() { return maxPerHour; }
        public void setMaxPerHour(int maxPerHour) { this.maxPerHour = maxPerHour; }
    }

    public static class Admin {
        private String basePath = "/api/ama/admin";

        public String getBasePath() { return basePath; }
        public void setBasePath(String basePath) { this.basePath = basePath; }
    }

    public static class Jdbc {
        private boolean ddlEnabled = true;
        private String tablePrefix = "ama_";
        /** Optional schema name (e.g. {@code platform}) to qualify tables with. */
        private String schema = "";

        public boolean isDdlEnabled() { return ddlEnabled; }
        public void setDdlEnabled(boolean ddlEnabled) { this.ddlEnabled = ddlEnabled; }
        public String getTablePrefix() { return tablePrefix; }
        public void setTablePrefix(String tablePrefix) { this.tablePrefix = tablePrefix; }
        public String getSchema() { return schema; }
        public void setSchema(String schema) { this.schema = schema; }
    }

    public static class Providers {
        private ProviderConfig openai = ProviderConfig.forOpenAi();
        private ProviderConfig anthropic = ProviderConfig.forAnthropic();
        private ProviderConfig gemini = ProviderConfig.forGemini();
        private ProviderConfig ollama = ProviderConfig.forOllama();

        public ProviderConfig getOpenai() { return openai; }
        public void setOpenai(ProviderConfig openai) { this.openai = openai; }
        public ProviderConfig getAnthropic() { return anthropic; }
        public void setAnthropic(ProviderConfig anthropic) { this.anthropic = anthropic; }
        public ProviderConfig getGemini() { return gemini; }
        public void setGemini(ProviderConfig gemini) { this.gemini = gemini; }
        public ProviderConfig getOllama() { return ollama; }
        public void setOllama(ProviderConfig ollama) { this.ollama = ollama; }
    }

    public static class ProviderConfig {
        private String name;
        private boolean enabled;
        private String apiKey = "";
        private String model;
        private String baseUrl;
        private int timeoutMs = 30000;
        /** Optional override of the system prompt used for this provider. */
        private String systemPrompt = "";

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey == null ? "" : apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public int getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
        public String getSystemPrompt() { return systemPrompt; }
        public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt == null ? "" : systemPrompt; }

        static ProviderConfig forOpenAi() {
            ProviderConfig c = new ProviderConfig();
            c.name = "openai";
            c.apiKey = env("AMA_OPENAI_API_KEY");
            c.model = "gpt-4o-mini";
            c.baseUrl = "https://api.openai.com/v1";
            return c;
        }

        static ProviderConfig forAnthropic() {
            ProviderConfig c = new ProviderConfig();
            c.name = "anthropic";
            c.apiKey = env("AMA_ANTHROPIC_API_KEY");
            c.model = "claude-3-5-haiku-latest";
            c.baseUrl = "https://api.anthropic.com/v1";
            return c;
        }

        static ProviderConfig forGemini() {
            ProviderConfig c = new ProviderConfig();
            c.name = "gemini";
            c.apiKey = env("AMA_GEMINI_API_KEY");
            c.model = "gemini-1.5-flash";
            c.baseUrl = "https://generativelanguage.googleapis.com/v1beta";
            return c;
        }

        static ProviderConfig forOllama() {
            ProviderConfig c = new ProviderConfig();
            c.name = "ollama";
            c.apiKey = "";
            c.model = "llama3.2";
            c.baseUrl = "http://localhost:11434";
            return c;
        }

        private static String env(String key) {
            String v = System.getenv(key);
            return v == null ? "" : v;
        }
    }
}