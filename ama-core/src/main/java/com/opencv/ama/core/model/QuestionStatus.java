package com.opencv.ama.core.model;

/**
 * Lifecycle of an Ask Me Anything question.
 *
 * <ul>
 *   <li>{@link #NEW} — created, not yet processed.</li>
 *   <li>{@link #DRAFT} — an AI answer exists but is hidden pending admin approval.</li>
 *   <li>{@link #PENDING} — queued for a human answer (no publishable answer yet).</li>
 *   <li>{@link #PUBLISHED} — a visible answer exists; the asker may fetch it.</li>
 *   <li>{@link #REJECTED} — admin declined the question.</li>
 *   <li>{@link #ARCHIVED} — hidden from default lists; used for cleanup.</li>
 * </ul>
 */
public enum QuestionStatus {
    NEW,
    DRAFT,
    PENDING,
    PUBLISHED,
    REJECTED,
    ARCHIVED
}
