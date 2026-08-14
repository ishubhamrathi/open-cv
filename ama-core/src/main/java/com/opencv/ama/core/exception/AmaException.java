package com.opencv.ama.core.exception;

/** Base type for all engine exceptions. */
public class AmaException extends RuntimeException {

    public AmaException(String message) {
        super(message);
    }

    public AmaException(String message, Throwable cause) {
        super(message, cause);
    }
}
