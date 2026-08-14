package com.opencv.ama.core.exception;

/** Thrown when an {@link com.opencv.ama.core.spi.AnswerProvider} cannot produce an answer. */
public class ProviderException extends AmaException {

    public ProviderException(String message) {
        super(message);
    }

    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
