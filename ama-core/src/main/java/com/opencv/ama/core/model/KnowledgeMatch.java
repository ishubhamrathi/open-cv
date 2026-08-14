package com.opencv.ama.core.model;

import java.util.List;
import java.util.Objects;

/** A knowledge base hit plus its computed confidence score. */
public final class KnowledgeMatch {

    private final KnowledgeEntry entry;
    private final double confidence;

    public KnowledgeMatch(KnowledgeEntry entry, double confidence) {
        this.entry = Objects.requireNonNull(entry);
        this.confidence = confidence;
    }

    public KnowledgeEntry entry() { return entry; }
    public double confidence() { return confidence; }
}
