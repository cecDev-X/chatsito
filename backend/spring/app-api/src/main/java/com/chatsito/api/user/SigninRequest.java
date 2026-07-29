package com.chatsito.api.user;

import jakarta.validation.constraints.NotNull;

public record SigninRequest(
        @NotNull String email,
        @NotNull String password) {
}
