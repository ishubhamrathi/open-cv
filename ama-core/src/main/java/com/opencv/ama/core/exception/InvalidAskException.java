package com.opencv.ama.core.exception;

/** Thrown when the asker submits an invalid question (blank, too long, unknown mode, ...). */
public class InvalidAskException extends AmaException {

    public InvalidAskException(String message) {
        super(message);
    }
}
