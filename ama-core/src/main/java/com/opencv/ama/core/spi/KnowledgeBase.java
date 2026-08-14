package com.opencv.ama.core.spi;

import com.opencv.ama.core.model.KnowledgeMatch;

/**
 * Minimal read-only view over the knowledge base used by the engine to enrich AI prompts
 * and to answer questions directly in {@code KNOWLEDGE_FIRST} mode.
 */
public interface KnowledgeBase {

    /**
     * @param query     free-text question
     * @param limit     max results
     * @param threshold minimum confidence (0.0–1.0) for a result to be returned
     */
    java.util.List<KnowledgeMatch> searchKnowledge(String query, int limit, double threshold);
}
