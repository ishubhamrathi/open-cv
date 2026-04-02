package com.portfolio.util

/**
 * Application-wide constants
 */
object AppConstants {

    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
    const val CHAT_HISTORY_LIMIT = 50

    // Confidence thresholds
    const val MIN_CONFIDENCE_THRESHOLD = 0.3
    const val HIGH_CONFIDENCE_THRESHOLD = 0.8

    // Categories
    val VALID_CATEGORIES = listOf(
        "skills",
        "experience",
        "projects",
        "education",
        "contact",
        "general"
    )

    // Cache configuration
    const val KNOWLEDGE_CACHE_NAME = "knowledgeItems"
    const val KNOWLEDGE_CACHE_TTL_SECONDS = 300L

    // API paths
    const val API_BASE_PATH = "/api"
    const val CHAT_ENDPOINT = "$API_BASE_PATH/chat"
    const val KNOWLEDGE_ENDPOINT = "$API_BASE_PATH/knowledge"

    // Security
    const val JWT_EXPIRATION_HOURS = 24L
    const val RATE_LIMIT_REQUESTS_PER_MINUTE = 60

    // File upload
    const val MAX_FILE_SIZE_MB = 10L
    const val ALLOWED_FILE_TYPES = setOf("pdf", "doc", "docx", "txt")

    // Session
    const val DEFAULT_SESSION_ID = "default"
    const val SESSION_PREFIX = "session_"
}
