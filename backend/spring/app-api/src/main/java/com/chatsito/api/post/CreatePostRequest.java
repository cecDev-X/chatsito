package com.chatsito.api.post;

import jakarta.validation.constraints.NotNull;

public record CreatePostRequest(
        @NotNull String message,
        @NotNull String selectedFile,
        @NotNull String title) {
}
