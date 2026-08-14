package com.opencv.ama.core.spi;

import com.opencv.ama.core.model.Answer;
import com.opencv.ama.core.model.KnowledgeEntry;
import com.opencv.ama.core.model.KnowledgeMatch;
import com.opencv.ama.core.model.NewQuestion;
import com.opencv.ama.core.model.Question;
import com.opencv.ama.core.model.QuestionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistence boundary of the engine. The engine never knows the storage technology —
 * the Spring Boot starter ships a JDBC implementation and host applications may provide
 * their own (e.g. jOOQ/PostgreSQL) implementation instead.
 */
public interface AmaStore {

    // ---- questions ----
    Question createQuestion(NewQuestion q, Instant now);

    Optional<Question> findQuestionById(String id);

    Optional<Question> findQuestionByReference(String reference);

    List<Question> listQuestions(QuestionFilter filter);

    long countQuestions(QuestionFilter filter);

    Question setQuestionStatus(String id, QuestionStatus status, Instant now);

    boolean deleteQuestion(String id);

    // ---- answers ----
    Answer saveAnswer(Answer answer);

    Optional<Answer> findAnswerByQuestionId(String questionId);

    // ---- knowledge base ----
    List<KnowledgeMatch> searchKnowledge(String query, int limit, double threshold);

    /**
     * Quick lookup for a near-exact question match (normalized equality or a keyword/alias
     * hit). Designed for instant answers to short, common inputs such as greetings and
     * suggested questions — returns at most one {@link KnowledgeMatch} with full confidence
     * when found, otherwise {@link Optional#empty()}.
     */
    Optional<KnowledgeMatch> findQuickAnswer(String question);

    List<KnowledgeEntry> listKnowledge();

    KnowledgeEntry createKnowledge(KnowledgeEntry entry);

    Optional<KnowledgeEntry> updateKnowledge(String id, KnowledgeEntry entry);

    boolean deleteKnowledge(String id);

    long countKnowledge();

    // ---- analytics ----
    Map<QuestionStatus, Long> statusCounts();

    long countPublishedSince(Instant since);
}
