package com.portfolio.exception

/**
 * Base exception for all portfolio-related errors
 */
abstract class PortfolioException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Thrown when a requested resource is not found
 */
class ResourceNotFoundException(
    resourceType: String,
    id: Any
) : PortfolioException(
    message = "$resourceType not found with id: $id",
    errorCode = "RESOURCE_NOT_FOUND"
)

/**
 * Thrown when validation fails
 */
class ValidationException(
    message: String,
    val fieldErrors: Map<String, String> = emptyMap()
) : PortfolioException(
    message = message,
    errorCode = "VALIDATION_ERROR"
)

/**
 * Thrown when an invalid category is provided
 */
class InvalidCategoryException(
    category: String,
    validCategories: List<String>
) : PortfolioException(
    message = "Invalid category: $category. Valid categories are: ${validCategories.joinToString()}",
    errorCode = "INVALID_CATEGORY"
)

/**
 * Thrown when chat input is invalid
 */
class InvalidChatInputException(
    message: String = "Invalid chat input"
) : PortfolioException(
    message = message,
    errorCode = "INVALID_CHAT_INPUT"
)

/**
 * Thrown when a duplicate resource exists
 */
class DuplicateResourceException(
    resourceType: String,
    identifier: String
) : PortfolioException(
    message = "$resourceType already exists with identifier: $identifier",
    errorCode = "DUPLICATE_RESOURCE"
)

/**
 * Thrown when an operation is not permitted
 */
class AccessDeniedException(
    message: String = "Access denied"
) : PortfolioException(
    message = message,
    errorCode = "ACCESS_DENIED"
)

/**
 * Global exception handler configuration
 */
