package com.opencv.ama.core.engine;

/** Generates short, URL-safe public references for questions (used to look up answers without auth). */
public interface ReferenceGenerator {

    String next();
}
