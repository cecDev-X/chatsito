package com.chatsito.api.user;

public record AuthResponse(AuthUserResponse result, String token) {
}
