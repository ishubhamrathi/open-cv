package com.opencv.ama.starter.api;

import com.opencv.ama.core.exception.AmaException;
import com.opencv.ama.core.exception.InvalidAskException;
import com.opencv.ama.core.exception.ProviderUnavailableException;
import com.opencv.ama.core.exception.QuestionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps engine exceptions to HTTP responses. Host apps may override with their own advice. */
@RestControllerAdvice
public class AmaExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AmaExceptionHandler.class);

    @ExceptionHandler(InvalidAskException.class)
    public ResponseEntity<ApiDtos.ErrorResponse> badRequest(InvalidAskException e) {
        return ResponseEntity.badRequest().body(new ApiDtos.ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiDtos.ErrorResponse> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ApiDtos.ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<ApiDtos.ErrorResponse> notFound(QuestionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiDtos.ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(ProviderUnavailableException.class)
    public ResponseEntity<ApiDtos.ErrorResponse> providerUnavailable(ProviderUnavailableException e) {
        log.warn("No provider available: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiDtos.ErrorResponse("Answering is temporarily unavailable. Please try again later."));
    }

    @ExceptionHandler(AmaException.class)
    public ResponseEntity<ApiDtos.ErrorResponse> amaError(AmaException e) {
        log.warn("AMA request failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiDtos.ErrorResponse(e.getMessage()));
    }
}