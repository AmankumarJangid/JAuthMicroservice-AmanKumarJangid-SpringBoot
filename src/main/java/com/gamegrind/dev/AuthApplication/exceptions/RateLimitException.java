package com.gamegrind.dev.AuthApplication.exceptions;

public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException() {
        super("RateLimitExceeded");
    }
}
