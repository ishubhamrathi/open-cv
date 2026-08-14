package com.opencv.ama.core.exception;

/** Thrown when a question/reference or knowledge entry does not exist. */
public class QuestionNotFoundException extends AmaException {

    public QuestionNotFoundException(String message) {
        super(message);
    }
}
