package com.opencv.ama.core.spi;

import com.opencv.ama.core.model.QuestionStatus;
import com.opencv.ama.core.model.WorkflowMode;

import java.util.Collection;

/**
 * Filter for listing questions. Every field is optional; empty/null values mean "all".
 *
 * @param statuses filter by one or more statuses (empty = all)
 * @param modes    filter by one or more workflow modes (empty = all)
 * @param query    loose text search over the question text (null/blank = all)
 * @param page     zero-based page number
 * @param size     page size (clamped by the store)
 */
public record QuestionFilter(
        Collection<QuestionStatus> statuses,
        Collection<WorkflowMode> modes,
        String query,
        int page,
        int size
) {
    public QuestionFilter {
        statuses = statuses == null ? java.util.List.of() : java.util.List.copyOf(statuses);
        modes = modes == null ? java.util.List.of() : java.util.List.copyOf(modes);
        page = Math.max(page, 0);
    }
}