package com.portfolio.model

/**
 * Request DTO for chat messages
 */
data class ChatRequest(
    val message: String,
    val sessionId: String = "default"
)

/**
 * Response DTO for chat messages
 */
data class ChatResponse(
    val message: String,
    val confidence: Double,
    val matchedCategory: String?,
    val suggestions: List<String> = emptyList(),
    val timestamp: String
)

/**
 * DTO for creating or updating knowledge items
 */
data class KnowledgeItemDTO(
    val id: Long? = null,
    val category: String,
    val question: String,
    val answer: String,
    val patterns: List<String> = emptyList(),
    val confidence: Double = 1.0,
    val isActive: Boolean = true,
    val tags: String? = null,
    val metadata: String? = null
)

/**
 * DTO for admin authentication
 */
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val username: String,
    val expiresAt: String
)

/**
 * Statistics DTO
 */
data class PortfolioStats(
    val totalKnowledgeItems: Int,
    val activeKnowledgeItems: Int,
    val totalConversations: Int,
    val topCategories: Map<String, Int>,
    val recentQuestions: List<String>
)
