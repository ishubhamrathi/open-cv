package com.opencv.ama.core.engine;

import com.opencv.ama.core.model.WorkflowMode;

import java.util.List;

/** Tuning knobs for the engine. All fields have sane defaults. */
public record EngineConfig(
        WorkflowMode defaultMode,
        WorkflowMode aiMode,
        double knowledgeThreshold,
        int maxQuestionLength,
        int maxAskerNameLength,
        List<String> providerOrder
) {
    public EngineConfig {
        providerOrder = providerOrder == null ? List.of() : List.copyOf(providerOrder);
    }

    public static EngineConfig defaults() {
        return new EngineConfig(
                WorkflowMode.AUTO,
                WorkflowMode.AUTO,
                0.5,
                1000,
                120,
                List.of()
        );
    }

    public EngineConfig withDefaultMode(WorkflowMode mode) {
        return new EngineConfig(mode, aiMode, knowledgeThreshold, maxQuestionLength, maxAskerNameLength, providerOrder);
    }

    public EngineConfig withAiMode(WorkflowMode mode) {
        return new EngineConfig(defaultMode, mode, knowledgeThreshold, maxQuestionLength, maxAskerNameLength, providerOrder);
    }

    public EngineConfig withKnowledgeThreshold(double threshold) {
        return new EngineConfig(defaultMode, aiMode, threshold, maxQuestionLength, maxAskerNameLength, providerOrder);
    }

    public EngineConfig withLimits(int maxQuestionLength, int maxAskerNameLength) {
        return new EngineConfig(defaultMode, aiMode, knowledgeThreshold, maxQuestionLength, maxAskerNameLength, providerOrder);
    }

    public EngineConfig withProviderOrder(List<String> order) {
        return new EngineConfig(defaultMode, aiMode, knowledgeThreshold, maxQuestionLength, maxAskerNameLength, order);
    }
}
