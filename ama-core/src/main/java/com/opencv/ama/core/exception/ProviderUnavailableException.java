package com.opencv.ama.core.exception;

/** Thrown when every provider in the chain failed to produce an answer. */
public class ProviderUnavailableException extends AmaException {

    public ProviderUnavailableException(String message) {
        super(message);
    }

    public ProviderUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
