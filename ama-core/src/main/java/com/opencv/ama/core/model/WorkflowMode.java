package com.opencv.ama.core.model;

/**
 * Controls how an incoming question is turned into an answer.
 *
 * <ul>
 *   <li>{@link #AUTO} — the provider chain answers immediately and the answer is published.</li>
 *   <li>{@link #REVIEW} — the provider chain drafts an answer that stays hidden until an admin approves it.</li>
 *   <li>{@link #MANUAL} — no AI is invoked; the question is queued for an admin-written answer.</li>
 *   <li>{@link #KNOWLEDGE_FIRST} — the knowledge base is consulted first; if a match clears the
 *       confidence threshold it is published directly, otherwise the question follows the configured
 *       {@code aiMode} ({@link #AUTO} or {@link #REVIEW}).</li>
 * </ul>
 */
public enum WorkflowMode {
    AUTO,
    REVIEW,
    MANUAL,
    KNOWLEDGE_FIRST
}
