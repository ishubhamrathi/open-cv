package com.opencv.ama.core.engine;

import java.security.SecureRandom;

/**
 * Default reference generator producing 8-char URL-safe codes from an unambiguous alphabet
 * (no 0/O, 1/I/l). Collision avoidance is the caller's job.
 */
public class SecureReferenceGenerator implements ReferenceGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String next() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}