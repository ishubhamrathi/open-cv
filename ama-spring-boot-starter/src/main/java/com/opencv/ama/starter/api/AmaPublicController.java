package com.opencv.ama.starter.api;

import com.opencv.ama.core.engine.AmaEngine;
import com.opencv.ama.core.exception.InvalidAskException;
import com.opencv.ama.core.model.AskResult;
import com.opencv.ama.core.model.Question;
import com.opencv.ama.core.model.QuestionStatus;
import com.opencv.ama.core.model.WorkflowMode;
import com.opencv.ama.starter.rate.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

/** Public, unauthenticated surface of the Ask Me Anything engine. */
@RestController
@RequestMapping("/api/ama")
public class AmaPublicController {

    private final AmaEngine engine;
    private final RateLimiter rateLimiter;

    public AmaPublicController(AmaEngine engine, RateLimiter rateLimiter) {
        this.engine = engine;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/ask")
    public ResponseEntity<ApiDtos.AskResponse> ask(@RequestBody ApiDtos.AskRequest req,
                                                   HttpServletRequest http) {
        String question = req.question() == null ? "" : req.question().trim();
        if (question.isEmpty()) {
            throw new InvalidAskException("Question cannot be empty");
        }
        if (req.askerEmail() != null && !req.askerEmail().isBlank() && !req.askerEmail().contains("@")) {
            throw new InvalidAskException("Email looks invalid");
        }
        if (!rateLimiter.tryAcquire(clientIp(http))) {
            throw new InvalidAskException("Too many questions. Please try again later.");
        }

        WorkflowMode mode = parseMode(req.mode());
        AskResult result = engine.ask(question, req.askerName(), req.askerEmail(), req.category(), mode);

        Question q = result.question();
        boolean answered = result.answered();
        String answer = answered && result.answer() != null ? result.answer().content() : null;
        String message = switch (q.status()) {
            case PUBLISHED -> answered ? "Here is your answer." : "Question received.";
            case DRAFT -> "Your question is being answered and will appear here after review.";
            case PENDING -> "Thanks — your question is in the queue and will be answered soon.";
            case REJECTED -> "This question was declined.";
            default -> "Question received.";
        };

        return ResponseEntity.ok(new ApiDtos.AskResponse(
                q.reference(), q.id(), q.status(), q.mode(), answered, answer, message));
    }

    /** Poll for the status/answer of a submitted question by its short reference. */
    @GetMapping("/questions/{reference}")
    public ResponseEntity<ApiDtos.QuestionStatusResponse> status(@PathVariable("reference") String reference) {
        Question q = engine.questionByReference(reference)
                .orElseThrow(() -> new InvalidAskException("Unknown reference: " + reference));
        String answer = engine.publishedAnswer(q.reference()).map(a -> a.content()).orElse(null);
        return ResponseEntity.ok(new ApiDtos.QuestionStatusResponse(
                q.reference(), q.status(), q.mode(), q.question(), q.createdAt(), q.answeredAt(), answer));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiDtos.ProviderHealthResponse> health() {
        List<ApiDtos.ProviderHealthResponse.ProviderHealthItem> items =
                engine.providerHealth().stream()
                        .map(h -> new ApiDtos.ProviderHealthResponse.ProviderHealthItem(h.name(), h.available()))
                        .toList();
        return ResponseEntity.ok(new ApiDtos.ProviderHealthResponse(items));
    }

    static WorkflowMode parseMode(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return WorkflowMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidAskException("Unknown mode: " + raw);
        }
    }

    static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}