package com.opencv.ama.core.engine;

import com.opencv.ama.core.exception.InvalidAskException;
import com.opencv.ama.core.exception.ProviderUnavailableException;
import com.opencv.ama.core.exception.QuestionNotFoundException;
import com.opencv.ama.core.model.Answer;
import com.opencv.ama.core.model.AnswerSource;
import com.opencv.ama.core.model.AskResult;
import com.opencv.ama.core.model.KnowledgeEntry;
import com.opencv.ama.core.model.KnowledgeMatch;
import com.opencv.ama.core.model.NewQuestion;
import com.opencv.ama.core.model.Question;
import com.opencv.ama.core.model.QuestionStatus;
import com.opencv.ama.core.model.WorkflowMode;
import com.opencv.ama.core.spi.AmaStore;
import com.opencv.ama.core.spi.AnswerRequest;
import com.opencv.ama.core.spi.ProviderAnswer;
import com.opencv.ama.core.spi.QuestionFilter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Facade of the Ask Me Anything engine. Implements the four workflow modes and delegates
 * everything else to {@link AmaStore} and the {@link ProviderChain}.
 *
 * <p>Thread-safety: stateless apart from injected collaborators, which are safe to share.</p>
 */
public final class AmaEngine {

    private final AmaStore store;
    private final ProviderChain chain;
    private final EngineConfig config;
    private final ReferenceGenerator references;

    public AmaEngine(AmaStore store, ProviderChain chain, EngineConfig config, ReferenceGenerator references) {
        this.store = store;
        this.chain = chain;
        this.config = config;
        this.references = references;
    }

    // ------------------------------------------------------------------ ask

    public AskResult ask(String question, String askerName, String askerEmail, String category, WorkflowMode requestedMode) {
        String text = question == null ? "" : question.trim();
        if (text.isEmpty()) {
            throw new InvalidAskException("Question cannot be empty");
        }
        if (text.length() > config.maxQuestionLength()) {
            throw new InvalidAskException("Question exceeds maximum length of " + config.maxQuestionLength());
        }
        String name = safe(askerName, config.maxAskerNameLength());
        String email = safe(askerEmail, 200);

        WorkflowMode mode = requestedMode != null ? requestedMode : config.defaultMode();
        String reference = uniqueReference();
        Question q = store.createQuestion(
                new NewQuestion(java.util.UUID.randomUUID().toString(), reference, name, email,
                        text, normalizeCategory(category), mode),
                Instant.now());

        // Quick answers: predefined/curated knowledge hits resolve instantly without a
        // provider, keeping common inputs (greetings, suggested questions) instant.
        if (mode != WorkflowMode.MANUAL) {
            Optional<KnowledgeMatch> quick = store.findQuickAnswer(text);
            if (quick.isPresent()) {
                return publishKnowledgeAnswer(q, quick.get());
            }
        }

        return switch (mode) {
            case MANUAL -> {
                store.setQuestionStatus(q.id(), QuestionStatus.PENDING, Instant.now());
                yield AskResult.withoutAnswer(store.findQuestionById(q.id()).orElse(q));
            }
            case AUTO -> answerByAi(q, true);
            case REVIEW -> answerByAi(q, false);
            case KNOWLEDGE_FIRST -> {
                Optional<KnowledgeMatch> hit = store.searchKnowledge(text, 1, config.knowledgeThreshold())
                        .stream().findFirst();
                if (hit.isPresent() && hit.get().confidence() >= config.knowledgeThreshold()) {
                    yield publishKnowledgeAnswer(q, hit.get());
                }
                yield answerByAi(q, config.aiMode() == WorkflowMode.REVIEW);
            }
        };
    }

    private AskResult answerByAi(Question q, boolean publish) {
        List<String> context = store.searchKnowledge(q.question(), 3, 0.0)
                .stream()
                .map(m -> "- Q: " + m.entry().question() + "\n  A: " + m.entry().answer())
                .toList();
        try {
            ProviderAnswer pa = chain.answer(new AnswerRequest(q.question(), q.askerName(), context));
            Answer answer = new Answer(
                    java.util.UUID.randomUUID().toString(),
                    q.id(),
                    pa.content(),
                    AnswerSource.AI,
                    pa.providerName(),
                    pa.model(),
                    pa.confidence(),
                    Instant.now(),
                    null,
                    null);
            store.saveAnswer(answer);
            if (publish) {
                Question published = store.setQuestionStatus(q.id(), QuestionStatus.PUBLISHED, Instant.now());
                return AskResult.answered(published, store.findAnswerByQuestionId(q.id()).orElse(answer));
            }
            Question drafted = store.setQuestionStatus(q.id(), QuestionStatus.DRAFT, Instant.now());
            return AskResult.withoutAnswer(drafted);
        } catch (ProviderUnavailableException e) {
            Question pending = store.setQuestionStatus(q.id(), QuestionStatus.PENDING, Instant.now());
            return AskResult.withoutAnswer(pending);
        }
    }

    private AskResult publishKnowledgeAnswer(Question q, KnowledgeMatch match) {
        Answer answer = new Answer(
                java.util.UUID.randomUUID().toString(),
                q.id(),
                match.entry().answer(),
                AnswerSource.KNOWLEDGE,
                "knowledge-base",
                null,
                match.confidence(),
                Instant.now(),
                null,
                null);
        store.saveAnswer(answer);
        Question published = store.setQuestionStatus(q.id(), QuestionStatus.PUBLISHED, Instant.now());
        return AskResult.answered(published, store.findAnswerByQuestionId(q.id()).orElse(answer));
    }

    // --------------------------------------------------------------- reads

    public Optional<Question> questionByReference(String reference) {
        return reference == null ? Optional.empty() : store.findQuestionByReference(reference);
    }

    /** Published answer for a reference, or empty when not yet available. */
    public Optional<Answer> publishedAnswer(String reference) {
        return questionByReference(reference)
                .filter(q -> q.status() == QuestionStatus.PUBLISHED)
                .flatMap(q -> store.findAnswerByQuestionId(q.id()));
    }

    /** Latest answer for a question regardless of publication state (admin review use). */
    public Optional<Answer> latestAnswer(String questionId) {
        return store.findAnswerByQuestionId(questionId);
    }

    public List<Question> listQuestions(QuestionFilter filter) {
        return store.listQuestions(filter);
    }

    public long countQuestions(QuestionFilter filter) {
        return store.countQuestions(filter);
    }

    // ----------------------------------------------------------- admin ops

    /**
     * Approve an existing AI draft for a question (optionally with an edited answer text).
     * When {@code editedAnswer} is provided the AI draft is discarded in favour of a HUMAN answer.
     */
    public Question approveDraft(String questionId, String editedAnswer, String approvedBy) {
        Question q = store.findQuestionById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found: " + questionId));
        Instant now = Instant.now();
        if (editedAnswer != null && !editedAnswer.isBlank()) {
            Answer human = new Answer(java.util.UUID.randomUUID().toString(), q.id(), editedAnswer.trim(),
                    AnswerSource.HUMAN, "admin", null, null, now, approvedBy, now);
            store.saveAnswer(human);
        } else {
            store.findAnswerByQuestionId(q.id()).ifPresent(a -> store.saveAnswer(a.withApproval(approvedBy, now)));
        }
        return store.setQuestionStatus(q.id(), QuestionStatus.PUBLISHED, now);
    }

    /** Publish a human-written answer for a question (overwrites any existing answer). */
    public Question publishManualAnswer(String questionId, String content, String approvedBy) {
        Question q = store.findQuestionById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found: " + questionId));
        if (content == null || content.isBlank()) {
            throw new InvalidAskException("Answer cannot be empty");
        }
        Instant now = Instant.now();
        Answer human = new Answer(java.util.UUID.randomUUID().toString(), q.id(), content.trim(),
                AnswerSource.HUMAN, "admin", null, null, now, approvedBy, now);
        store.saveAnswer(human);
        return store.setQuestionStatus(q.id(), QuestionStatus.PUBLISHED, now);
    }

    public Question setStatus(String questionId, QuestionStatus status) {
        Question q = store.findQuestionById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found: " + questionId));
        return store.setQuestionStatus(q.id(), status, Instant.now());
    }

    public QuestionStatus statusOf(String questionId) {
        return store.findQuestionById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found: " + questionId))
                .status();
    }

    public boolean deleteQuestion(String id) {
        return store.deleteQuestion(id);
    }

    // ----------------------------------------------------- knowledge base

    public List<KnowledgeMatch> searchKnowledge(String query, int limit, double threshold) {
        return store.searchKnowledge(query, limit, threshold);
    }

    public List<KnowledgeEntry> listKnowledge() {
        return store.listKnowledge();
    }

    public KnowledgeEntry createKnowledge(String category, String question, String answer,
                                          List<String> keywords, Double confidence) {
        if (question == null || question.isBlank() || answer == null || answer.isBlank()) {
            throw new InvalidAskException("Knowledge question and answer are required");
        }
        Instant now = Instant.now();
        return store.createKnowledge(new KnowledgeEntry(
                java.util.UUID.randomUUID().toString(),
                normalizeCategory(category),
                question.trim(),
                answer.trim(),
                keywords,
                confidence,
                true,
                now,
                now));
    }

    public Optional<KnowledgeEntry> updateKnowledge(String id, String category, String question, String answer,
                                                    List<String> keywords, Double confidence, Boolean active) {
        KnowledgeEntry existing = store.listKnowledge().stream()
                .filter(e -> e.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new QuestionNotFoundException("Knowledge entry not found: " + id));
        KnowledgeEntry updated = new KnowledgeEntry(
                id,
                normalizeCategory(category != null ? category : existing.category()),
                question != null ? question.trim() : existing.question(),
                answer != null ? answer.trim() : existing.answer(),
                keywords != null ? keywords : existing.keywords(),
                confidence != null ? confidence : existing.confidence(),
                active != null ? active : existing.active(),
                existing.createdAt(),
                Instant.now());
        return store.updateKnowledge(id, updated);
    }

    public boolean deleteKnowledge(String id) {
        return store.deleteKnowledge(id);
    }

    // -------------------------------------------------------- diagnostics

    public Map<QuestionStatus, Long> statusCounts() {
        return store.statusCounts();
    }

    public long publishedSince(Instant since) {
        return store.countPublishedSince(since);
    }

    public List<ProviderHealth> providerHealth() {
        return chain.providers().stream()
                .map(p -> new ProviderHealth(p.name(), p.isAvailable()))
                .toList();
    }

    public List<String> order() {
        return chain.order();
    }

    public record ProviderHealth(String name, boolean available) {}

    // -------------------------------------------------------------- utils

    private String uniqueReference() {
        String candidate;
        do {
            candidate = references.next();
        } while (store.findQuestionByReference(candidate).isPresent());
        return candidate;
    }

    private static String safe(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "general";
        }
        String c = category.trim().toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_-]", "-");
        return c.length() > 40 ? c.substring(0, 40) : c;
    }
}