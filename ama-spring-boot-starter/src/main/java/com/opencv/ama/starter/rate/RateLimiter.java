package com.opencv.ama.starter.rate;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Minimal in-memory sliding-window rate limiter keyed by an arbitrary string (e.g. client IP).
 * Not suitable for multi-instance deployments — fine for a single portfolio site.
 */
public class RateLimiter {

    private final AtomicBoolean enabled;
    private final int maxRequests;
    private final Duration window;
    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    public RateLimiter(boolean enabled, int maxRequests, Duration window) {
        this.enabled = new AtomicBoolean(enabled);
        this.maxRequests = Math.max(1, maxRequests);
        this.window = window;
    }

    public boolean tryAcquire(String key) {
        if (!enabled.get()) {
            return true;
        }
        long cutoff = Instant.now().minus(window).toEpochMilli();
        Deque<Instant> deque = hits.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst().toEpochMilli() < cutoff) {
                deque.pollFirst();
            }
            if (deque.size() >= maxRequests) {
                return false;
            }
            deque.addLast(Instant.now());
            return true;
        }
    }
}