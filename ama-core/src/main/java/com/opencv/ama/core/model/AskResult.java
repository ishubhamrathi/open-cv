package com.opencv.ama.core.model;

/**
 * Result of submitting a question.
 * <ul>
 *   <li>answered — a published answer exists right now (AUTO / KNOWLEDGE_FIRST hit).</li>
 *   <li>draft   — an AI draft is waiting for admin approval (REVIEW / KNOWLEDGE_FIRST-&gt;AI).</li>
 *   <li>pending — queued for a human answer (MANUAL / provider failure).</li>
 * </ul>
 */
public final class AskResult {

    private final Question question;
    private final Answer answer;

    private AskResult(Question question, Answer answer) {
        this.question = question;
        this.answer = answer;
    }

    public static AskResult answered(Question q, Answer a) {
        return new AskResult(q, a);
    }

    public static AskResult withoutAnswer(Question q) {
        return new AskResult(q, null);
    }

    public Question question() { return question; }
    public Answer answer() { return answer; }

    public boolean answered() { return answer != null && question.status() == QuestionStatus.PUBLISHED; }
}
