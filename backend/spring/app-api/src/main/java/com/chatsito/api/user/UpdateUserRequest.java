package com.chatsito.api.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotNull String name,
        @NotNull String bio,
        @NotNull String imageUrl) {
}
