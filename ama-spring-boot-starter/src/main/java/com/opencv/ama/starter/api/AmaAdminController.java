package com.opencv.ama.starter.api;

import com.opencv.ama.core.engine.AmaEngine;
import com.opencv.ama.core.exception.InvalidAskException;
import com.opencv.ama.core.exception.QuestionNotFoundException;
import com.opencv.ama.core.model.Answer;
import com.opencv.ama.core.model.KnowledgeEntry;
import com.opencv.ama.core.model.Question;
import com.opencv.ama.core.model.QuestionStatus;
import com.opencv.ama.core.model.WorkflowMode;
import com.opencv.ama.core.spi.QuestionFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Admin review + curation surface. Intended to be protected by the host app's security
 * (the demo app backs it with basic auth; production hosts should use their own).
 */
@RestController
@RequestMapping("${ama.admin.base-path:/api/ama/admin}")
public class AmaAdminController {

    private final AmaEngine engine;

    public AmaAdminController(AmaEngine engine) {
        this.engine = engine;
    }

    // ------------------------------------------------------------ questions

    @GetMapping("/questions")
    public ApiDtos.QuestionListResponse list(
            @RequestParam(required = false) Set<String> status,
            @RequestParam(required = false) Set<String> mode,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        QuestionFilter filter = new QuestionFilter(parseStatuses(status), parseModes(mode), q, page, size);
        List<ApiDtos.QuestionView> items =
                engine.listQuestions(filter).stream().map(this::toView).toList();
        return new ApiDtos.QuestionListResponse(items, engine.countQuestions(filter), page, size);
    }

    @GetMapping("/questions/{id}")
    public ApiDtos.QuestionView get(@PathVariable String id) {
        return toView(requireQuestion(id));
    }

    /** Approve an AI draft (optionally with an edited answer). */
    @PostMapping("/questions/{id}/approve")
    public ApiDtos.QuestionView approve(@PathVariable String id,
                                        @RequestBody(required = false) ApiDtos.ApproveRequest body) {
        String approvedBy = body == null || body.approvedBy() == null || body.approvedBy().isBlank()
                ? "admin" : body.approvedBy();
        return toView(engine.approveDraft(id, body == null ? null : body.editedAnswer(), approvedBy));
    }

    /** Publish a human-written answer immediately. */
    @PostMapping("/questions/{id}/answer")
    public ApiDtos.QuestionView answer(@PathVariable String id, @RequestBody ApiDtos.AnswerRequest body) {
        String approvedBy = body.approvedBy() == null || body.approvedBy().isBlank() ? "admin" : body.approvedBy();
        return toView(engine.publishManualAnswer(id, body.content(), approvedBy));
    }

    @PostMapping("/questions/{id}/reject")
    public ApiDtos.QuestionView reject(@PathVariable String id) {
        return toView(engine.setStatus(requireQuestion(id).id(), QuestionStatus.REJECTED));
    }

    @PostMapping("/questions/{id}/archive")
    public ApiDtos.QuestionView archive(@PathVariable String id) {
        return toView(engine.setStatus(requireQuestion(id).id(), QuestionStatus.ARCHIVED));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        engine.deleteQuestion(requireQuestion(id).id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ApiDtos.StatsResponse stats() {
        return new ApiDtos.StatsResponse(
                engine.statusCounts().values().stream().mapToLong(Long::longValue).sum(),
                engine.statusCounts(),
                engine.publishedSince(Instant.now().minus(7, ChronoUnit.DAYS)));
    }

    @GetMapping("/providers")
    public ApiDtos.ProviderHealthResponse providers() {
        List<ApiDtos.ProviderHealthResponse.ProviderHealthItem> items =
                engine.providerHealth().stream()
                        .map(h -> new ApiDtos.ProviderHealthResponse.ProviderHealthItem(h.name(), h.available()))
                        .toList();
        return new ApiDtos.ProviderHealthResponse(items);
    }

    // ------------------------------------------------------------ knowledge

    @GetMapping("/knowledge")
    public List<ApiDtos.KnowledgeView> knowledge() {
        return engine.listKnowledge().stream().map(AmaAdminController::toKnowledge).toList();
    }

    @PostMapping("/knowledge")
    public ApiDtos.KnowledgeView createKnowledge(@RequestBody ApiDtos.KnowledgeUpsert body) {
        return toKnowledge(engine.createKnowledge(
                body.category(), body.question(), body.answer(), body.keywords(), body.confidence()));
    }

    @PutMapping("/knowledge/{id}")
    public ApiDtos.KnowledgeView updateKnowledge(@PathVariable String id, @RequestBody ApiDtos.KnowledgeUpsert body) {
        return toKnowledge(engine.updateKnowledge(
                        id, body.category(), body.question(), body.answer(),
                        body.keywords(), body.confidence(), body.active())
                .orElseThrow(() -> new QuestionNotFoundException("Knowledge entry not found: " + id)));
    }

    @DeleteMapping("/knowledge/{id}")
    public ResponseEntity<Void> deleteKnowledge(@PathVariable String id) {
        if (!engine.deleteKnowledge(id)) {
            throw new QuestionNotFoundException("Knowledge entry not found: " + id);
        }
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------ helpers

    private Question requireQuestion(String id) {
        return engine.listQuestions(new QuestionFilter(null, null, null, 0, 1000))
                .stream()
                .filter(x -> x.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new QuestionNotFoundException("Question not found: " + id));
    }

    private ApiDtos.QuestionView toView(Question q) {
        Answer answer = engine.latestAnswer(q.id()).orElse(null);
        return new ApiDtos.QuestionView(
                q.id(), q.reference(), q.askerName(), q.askerEmail(), q.question(), q.category(),
                q.mode(), q.status(), q.createdAt(), q.answeredAt(), toAnswerView(answer));
    }

    private static ApiDtos.AnswerView toAnswerView(Answer a) {
        if (a == null) {
            return null;
        }
        return new ApiDtos.AnswerView(
                a.content(),
                a.source() == null ? null : a.source().name(),
                a.provider(), a.model(), a.confidence(), a.approvedBy(), a.approvedAt());
    }

    private static ApiDtos.KnowledgeView toKnowledge(KnowledgeEntry e) {
        return new ApiDtos.KnowledgeView(
                e.id(), e.category(), e.question(), e.answer(), e.keywords(), e.confidence(), e.active());
    }

    private static Collection<QuestionStatus> parseStatuses(Set<String> raw) {
        return raw == null || raw.isEmpty() ? List.of()
                : raw.stream().map(s -> QuestionStatus.valueOf(s.toUpperCase(Locale.ROOT))).toList();
    }

    private static Collection<WorkflowMode> parseModes(Set<String> raw) {
        return raw == null || raw.isEmpty() ? List.of()
                : raw.stream().map(s -> WorkflowMode.valueOf(s.toUpperCase(Locale.ROOT))).toList();
    }
}