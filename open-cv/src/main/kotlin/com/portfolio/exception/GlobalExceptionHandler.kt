package com.portfolio.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Global exception handler for REST controllers.
 * Provides consistent error responses across the application.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        log.warn("Resource not found: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.errorCode, ex.message, HttpStatus.NOT_FOUND.value()))
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ErrorResponse> {
        log.warn("Validation failed: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.errorCode, ex.message, HttpStatus.BAD_REQUEST.value(), ex.fieldErrors))
    }

    /**
     * Handle invalid category exceptions
     */
    @ExceptionHandler(InvalidCategoryException::class)
    fun handleInvalidCategory(ex: InvalidCategoryException): ResponseEntity<ErrorResponse> {
        log.warn("Invalid category: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.errorCode, ex.message, HttpStatus.BAD_REQUEST.value()))
    }

    /**
     * Handle invalid chat input exceptions
     */
    @ExceptionHandler(InvalidChatInputException::class)
    fun handleInvalidChatInput(ex: InvalidChatInputException): ResponseEntity<ErrorResponse> {
        log.warn("Invalid chat input: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.errorCode, ex.message, HttpStatus.BAD_REQUEST.value()))
    }

    /**
     * Handle duplicate resource exceptions
     */
    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResource(ex: DuplicateResourceException): ResponseEntity<ErrorResponse> {
        log.warn("Duplicate resource: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(ex.errorCode, ex.message, HttpStatus.CONFLICT.value()))
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        log.warn("Access denied: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(ex.errorCode, ex.message, HttpStatus.FORBIDDEN.value()))
    }

    /**
     * Handle all other uncaught exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error occurred", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            ))
    }
}

/**
 * Standard error response structure
 */
data class ErrorResponse(
    val errorCode: String,
    val message: String,
    val status: Int,
    val fieldErrors: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
