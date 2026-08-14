package com.opencv.ama.starter.store;

import com.opencv.ama.core.model.Answer;
import com.opencv.ama.core.model.AnswerSource;
import com.opencv.ama.core.model.KnowledgeEntry;
import com.opencv.ama.core.model.KnowledgeMatch;
import com.opencv.ama.core.model.NewQuestion;
import com.opencv.ama.core.model.Question;
import com.opencv.ama.core.model.QuestionStatus;
import com.opencv.ama.core.model.WorkflowMode;
import com.opencv.ama.core.spi.AmaStore;
import com.opencv.ama.core.spi.QuestionFilter;
import com.opencv.ama.starter.config.AmaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * {@link AmaStore} implementation backed by {@link JdbcTemplate}. Works against any JDBC
 * database (H2 for the demo, PostgreSQL in production). When {@code ama.jdbc.ddl-enabled}
 * is true the three tables ({@code questions}, {@code answers}, {@code knowledge}) are created
 * on first use with the configured table prefix.
 *
 * <p>Table/column names are built only from the configured prefix/schema, validated against
 * a strict regex — never from user input.</p>
 */
public class AmaJdbcStore implements AmaStore {

    private static final Logger log = LoggerFactory.getLogger(AmaJdbcStore.class);
    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]*$");
    private static final int MAX_PAGE_SIZE = 100;

    private final JdbcTemplate jdbc;
    private final String questions;
    private final String answers;
    private final String knowledge;

    public AmaJdbcStore(JdbcTemplate jdbcTemplate, AmaProperties properties) {
        this.jdbc = jdbcTemplate;
        String prefix = properties.getJdbc().getTablePrefix();
        String schema = properties.getJdbc().getSchema();
        if (!IDENTIFIER.matcher(prefix).matches() || !IDENTIFIER.matcher(schema).matches()) {
            throw new IllegalArgumentException("ama.jdbc.table-prefix/schema must match [A-Za-z0-9_]*");
        }
        String q = schema.isBlank() ? prefix : schema + "." + prefix;
        this.questions = q + "questions";
        this.answers = q + "answers";
        this.knowledge = q + "knowledge";

        if (properties.getJdbc().isDdlEnabled()) {
            createTables();
        }
    }

    private void createTables() {
        String fk = foreignKeySql(answers, questions);
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + questions + " ("
                + "id VARCHAR(36) PRIMARY KEY,"
                + "reference VARCHAR(16) NOT NULL UNIQUE,"
                + "asker_name VARCHAR(120),"
                + "asker_email VARCHAR(200),"
                + "question TEXT NOT NULL,"
                + "category VARCHAR(40),"
                + "mode VARCHAR(16) NOT NULL,"
                + "status VARCHAR(16) NOT NULL,"
                + "created_at TIMESTAMP NOT NULL,"
                + "answered_at TIMESTAMP)");
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + answers + " ("
                + "id VARCHAR(36) PRIMARY KEY,"
                + "question_id VARCHAR(36) NOT NULL UNIQUE,"
                + "content TEXT NOT NULL,"
                + "source VARCHAR(16) NOT NULL,"
                + "provider VARCHAR(40),"
                + "model VARCHAR(80),"
                + "confidence DOUBLE PRECISION,"
                + "created_at TIMESTAMP NOT NULL,"
                + "approved_by VARCHAR(80),"
                + "approved_at TIMESTAMP,"
                + fk + ")");
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + knowledge + " ("
                + "id VARCHAR(36) PRIMARY KEY,"
                + "category VARCHAR(40),"
                + "question VARCHAR(500) NOT NULL,"
                + "answer TEXT NOT NULL,"
                + "keywords TEXT,"
                + "confidence DOUBLE PRECISION,"
                + "active BOOLEAN NOT NULL DEFAULT TRUE,"
                + "created_at TIMESTAMP NOT NULL,"
                + "updated_at TIMESTAMP NOT NULL)");
        log.info("AMA store tables ensured: {}", String.join(", ", questions, answers, knowledge));
    }

    private String foreignKeySql(String child, String parent) {
        String name = ("fk_" + child + "_question").replace('.', '_');
        return "CONSTRAINT " + name + " FOREIGN KEY (question_id) REFERENCES " + parent + "(id) ON DELETE CASCADE";
    }

    // ------------------------------------------------------------------ questions

    @Override
    public Question createQuestion(NewQuestion q, Instant now) {
        LocalDateTime at = toLocal(now);
        jdbc.update("INSERT INTO " + questions
                        + " (id, reference, asker_name, asker_email, question, category, mode, status, created_at, answered_at)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NULL)",
                q.id(), q.reference(), q.askerName(), q.askerEmail(), q.question(), q.category(),
                q.mode().name(), QuestionStatus.NEW.name(), at);
        return new Question(q.id(), q.reference(), q.askerName(), q.askerEmail(), q.question(),
                q.category(), q.mode(), QuestionStatus.NEW, now, null);
    }

    @Override
    public Optional<Question> findQuestionById(String id) {
        return findQuestion("id", id);
    }

    @Override
    public Optional<Question> findQuestionByReference(String reference) {
        return findQuestion("reference", reference);
    }

    private Optional<Question> findQuestion(String column, String value) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(
                    "SELECT id, reference, asker_name, asker_email, question, category, mode, status, created_at, answered_at"
                            + " FROM " + questions + " WHERE " + column + " = ?",
                    QUESTION_MAPPER, value));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Question> listQuestions(QuestionFilter filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, reference, asker_name, asker_email, question, category, mode, status, created_at, answered_at"
                        + " FROM " + questions);
        List<Object> params = new ArrayList<>();
        appendFilter(sql, params, filter);
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize(filter.size()));
        params.add((long) filter.page() * pageSize(filter.size()));
        return jdbc.query(sql.toString(), QUESTION_MAPPER, params.toArray());
    }

    @Override
    public long countQuestions(QuestionFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + questions);
        List<Object> params = new ArrayList<>();
        appendFilter(sql, params, filter);
        Long count = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return count == null ? 0 : count;
    }

    private void appendFilter(StringBuilder sql, List<Object> params, QuestionFilter filter) {
        List<String> where = new ArrayList<>();
        if (filter.statuses() != null && !filter.statuses().isEmpty()) {
            where.add("status IN (" + placeholders(filter.statuses().size()) + ")");
            filter.statuses().forEach(s -> params.add(s.name()));
        }
        if (filter.modes() != null && !filter.modes().isEmpty()) {
            where.add("mode IN (" + placeholders(filter.modes().size()) + ")");
            filter.modes().forEach(m -> params.add(m.name()));
        }
        if (filter.query() != null && !filter.query().isBlank()) {
            where.add("LOWER(question) LIKE LOWER(?)");
            params.add("%" + escapeLike(filter.query().trim()) + "%");
        }
        if (!where.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", where));
        }
    }

    private static String placeholders(int n) {
        String[] p = new String[n];
        Arrays.fill(p, "?");
        return String.join(",", p);
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    @Override
    public boolean deleteQuestion(String id) {
        return jdbc.update("DELETE FROM " + questions + " WHERE id = ?", id) > 0;
    }

    private static int pageSize(int requested) {
        if (requested <= 0) {
            return 20;
        }
        return Math.min(requested, MAX_PAGE_SIZE);
    }

    @Override
    public Question setQuestionStatus(String id, QuestionStatus status, Instant now) {
        jdbc.update("UPDATE " + questions + " SET status = ?, answered_at = COALESCE(?, answered_at)"
                        + " WHERE id = ?",
                status.name(),
                status == QuestionStatus.PUBLISHED ? Timestamp.from(now) : null,
                id);
        return findQuestionById(id)
                .orElseThrow(() -> new IllegalStateException("Question disappeared: " + id));
    }

    // ------------------------------------------------------------------ answers

    @Override
    public Answer saveAnswer(Answer answer) {
        // One answer per question: delete-then-insert works on every dialect, unlike
        // PostgreSQL's ON CONFLICT (unsupported by H2) or H2's MERGE.
        jdbc.update("DELETE FROM " + answers + " WHERE question_id = ?", answer.questionId());
        jdbc.update("INSERT INTO " + answers
                        + " (id, question_id, content, source, provider, model, confidence, created_at, approved_by, approved_at)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                answer.id(), answer.questionId(), answer.content(), answer.source().name(), answer.provider(),
                answer.model(), answer.confidence(), Timestamp.from(answer.createdAt()), answer.approvedBy(),
                answer.approvedAt() == null ? null : Timestamp.from(answer.approvedAt()));
        return answer;
    }

    @Override
    public Optional<Answer> findAnswerByQuestionId(String questionId) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(
                    "SELECT id, question_id, content, source, provider, model, confidence, created_at, approved_by, approved_at"
                            + " FROM " + answers + " WHERE question_id = ?",
                    ANSWER_MAPPER, questionId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // ------------------------------------------------------------------ knowledge

    @Override
    public List<KnowledgeMatch> searchKnowledge(String query, int limit, double threshold) {
        List<String> tokens = tokenize(query);
        if (tokens.isEmpty()) {
            return List.of();
        }
        List<KnowledgeMatch> matches = new ArrayList<>();
        for (KnowledgeEntry entry : listKnowledgeInternal(true)) {
            double score = score(entry, tokens);
            if (score >= threshold) {
                matches.add(new KnowledgeMatch(entry, score));
            }
        }
        matches.sort((a, b) -> Double.compare(b.confidence(), a.confidence()));
        return matches.stream().limit(Math.max(0, limit)).toList();
    }

    @Override
    public Optional<KnowledgeMatch> findQuickAnswer(String question) {
        String normalized = normalizeForMatch(question);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        List<KnowledgeEntry> entries = listKnowledgeInternal(true);
        for (KnowledgeEntry entry : entries) {
            if (normalized.equals(normalizeForMatch(entry.question()))) {
                return Optional.of(new KnowledgeMatch(entry, 1.0));
            }
        }
        List<String> words = Arrays.stream(normalized.split(" "))
                .filter(w -> w.length() >= 2)
                .distinct()
                .toList();
        for (KnowledgeEntry entry : entries) {
            for (String keyword : entry.keywords()) {
                String kw = normalizeForMatch(keyword);
                if (kw.length() >= 2 && words.contains(kw)) {
                    return Optional.of(new KnowledgeMatch(entry, 1.0));
                }
            }
        }
        return Optional.empty();
    }

    private static String normalizeForMatch(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    private double score(KnowledgeEntry entry, List<String> tokens) {
        String searchable = (entry.question() + " " + entry.answer() + " " + String.join(" ", entry.keywords()))
                .toLowerCase(Locale.ROOT);
        long matched = tokens.stream().filter(searchable::contains).count();
        if (matched == 0) {
            return 0.0;
        }
        double base = (double) matched / tokens.size();
        long keywordBonus = tokens.stream()
                .filter(t -> entry.keywords().stream().anyMatch(k -> k.equalsIgnoreCase(t)))
                .count();
        return Math.min(1.0, base + keywordBonus * 0.15);
    }

    private static List<String> tokenize(String text) {
        if (text == null) {
            return List.of();
        }
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(t -> t.length() > 2)
                .distinct()
                .toList();
    }

    @Override
    public List<KnowledgeEntry> listKnowledge() {
        return listKnowledgeInternal(false);
    }

    private List<KnowledgeEntry> listKnowledgeInternal(boolean activeOnly) {
        String sql = "SELECT id, category, question, answer, keywords, confidence, active, created_at, updated_at"
                + " FROM " + knowledge + (activeOnly ? " WHERE active = TRUE" : "")
                + " ORDER BY created_at DESC";
        return jdbc.query(sql, KNOWLEDGE_MAPPER);
    }

    @Override
    public KnowledgeEntry createKnowledge(KnowledgeEntry entry) {
        jdbc.update("INSERT INTO " + knowledge
                        + " (id, category, question, answer, keywords, confidence, active, created_at, updated_at)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                entry.id(), entry.category(), entry.question(), entry.answer(),
                entry.keywords().isEmpty() ? null : String.join(",", entry.keywords()),
                entry.confidence(), entry.active(), toLocal(entry.createdAt()), toLocal(entry.updatedAt()));
        return entry;
    }

    @Override
    public Optional<KnowledgeEntry> updateKnowledge(String id, KnowledgeEntry entry) {
        int updated = jdbc.update("UPDATE " + knowledge
                        + " SET category = ?, question = ?, answer = ?, keywords = ?, confidence = ?, active = ?, updated_at = ?"
                        + " WHERE id = ?",
                entry.category(), entry.question(), entry.answer(),
                entry.keywords().isEmpty() ? null : String.join(",", entry.keywords()),
                entry.confidence(), entry.active(), toLocal(entry.updatedAt()), id);
        return updated > 0 ? Optional.of(entry) : Optional.empty();
    }

    @Override
    public boolean deleteKnowledge(String id) {
        return jdbc.update("DELETE FROM " + knowledge + " WHERE id = ?", id) > 0;
    }

    @Override
    public long countKnowledge() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM " + knowledge, Long.class);
        return count == null ? 0 : count;
    }

    // -------------------------------------------------------------- analytics

    @Override
    public Map<QuestionStatus, Long> statusCounts() {
        Map<QuestionStatus, Long> counts = new LinkedHashMap<>();
        for (QuestionStatus s : QuestionStatus.values()) {
            counts.put(s, 0L);
        }
        jdbc.query("SELECT status, COUNT(*) FROM " + questions + " GROUP BY status", rs -> {
            counts.put(QuestionStatus.valueOf(rs.getString(1).toUpperCase(Locale.ROOT)), rs.getLong(2));
        });
        return counts;
    }

    @Override
    public long countPublishedSince(Instant since) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + questions + " WHERE status = 'PUBLISHED' AND answered_at >= ?",
                Long.class, Timestamp.from(since));
        return count == null ? 0 : count;
    }

    // ------------------------------------------------------------------ mappers

    private static final RowMapper<Question> QUESTION_MAPPER = (rs, i) -> new Question(
            rs.getString("id"),
            rs.getString("reference"),
            rs.getString("asker_name"),
            rs.getString("asker_email"),
            rs.getString("question"),
            rs.getString("category"),
            WorkflowMode.valueOf(rs.getString("mode").toUpperCase(Locale.ROOT)),
            QuestionStatus.valueOf(rs.getString("status").toUpperCase(Locale.ROOT)),
            toInstant(rs.getTimestamp("created_at")),
            rs.getTimestamp("answered_at") == null ? null : toInstant(rs.getTimestamp("answered_at")));

    private static final RowMapper<Answer> ANSWER_MAPPER = (rs, i) -> new Answer(
            rs.getString("id"),
            rs.getString("question_id"),
            rs.getString("content"),
            AnswerSource.valueOf(rs.getString("source").toUpperCase(Locale.ROOT)),
            rs.getString("provider"),
            rs.getString("model"),
            (Double) rs.getObject("confidence"),
            toInstant(rs.getTimestamp("created_at")),
            rs.getString("approved_by"),
            rs.getTimestamp("approved_at") == null ? null : toInstant(rs.getTimestamp("approved_at")));

    private static final RowMapper<KnowledgeEntry> KNOWLEDGE_MAPPER = (rs, i) -> new KnowledgeEntry(
            rs.getString("id"),
            rs.getString("category"),
            rs.getString("question"),
            rs.getString("answer"),
            rs.getString("keywords") == null ? List.of()
                    : Arrays.stream(rs.getString("keywords").split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList(),
            (Double) rs.getObject("confidence"),
            rs.getBoolean("active"),
            toInstant(rs.getTimestamp("created_at")),
            toInstant(rs.getTimestamp("updated_at")));

    private static LocalDateTime toLocal(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}