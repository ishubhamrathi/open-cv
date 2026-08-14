package com.opencv.ama.starter.api;

import com.opencv.ama.core.model.QuestionStatus;
import com.opencv.ama.core.model.WorkflowMode;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Request/response shapes for the public and admin REST APIs. */
public final class ApiDtos {

    private ApiDtos() {
    }

    // ------------------------------------------------------------ public

    public record AskRequest(
            String question,
            String askerName,
            String askerEmail,
            String category,
            String mode
    ) {}

    public record AskResponse(
            String reference,
            String questionId,
            QuestionStatus status,
            WorkflowMode mode,
            boolean answered,
            String answer,
            String message
    ) {}

    public record QuestionStatusResponse(
            String reference,
            QuestionStatus status,
            WorkflowMode mode,
            String question,
            Instant createdAt,
            Instant answeredAt,
            String answer
    ) {}

    public record ProviderHealthResponse(List<ProviderHealthItem> providers) {
        public record ProviderHealthItem(String name, boolean available) {}
    }

    // ------------------------------------------------------------ admin

    public record QuestionView(
            String id,
            String reference,
            String askerName,
            String askerEmail,
            String question,
            String category,
            WorkflowMode mode,
            QuestionStatus status,
            Instant createdAt,
            Instant answeredAt,
            AnswerView answer
    ) {}

    public record AnswerView(
            String content,
            String source,
            String provider,
            String model,
            Double confidence,
            String approvedBy,
            Instant approvedAt
    ) {}

    public record QuestionListResponse(
            List<QuestionView> items,
            long total,
            int page,
            int size
    ) {}

    public record ApproveRequest(String editedAnswer, String approvedBy) {}

    public record AnswerRequest(String content, String approvedBy) {}

    public record StatsResponse(
            long total,
            Map<QuestionStatus, Long> byStatus,
            long publishedLast7Days
    ) {}

    // ------------------------------------------------------------ knowledge

    public record KnowledgeView(
            String id,
            String category,
            String question,
            String answer,
            List<String> keywords,
            Double confidence,
            boolean active
    ) {}

    public record KnowledgeUpsert(
            String category,
            String question,
            String answer,
            List<String> keywords,
            Double confidence,
            Boolean active
    ) {}

    public record ErrorResponse(String error) {}
}