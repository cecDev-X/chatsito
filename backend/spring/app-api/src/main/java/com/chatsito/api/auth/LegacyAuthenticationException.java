package com.chatsito.api.auth;

public class LegacyAuthenticationException extends RuntimeException {
    public LegacyAuthenticationException(String message) {
        super(message);
    }
}
