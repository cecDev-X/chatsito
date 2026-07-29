package com.chatsito.api.chat;

import jakarta.validation.constraints.NotNull;

public record SendMessageRequest(
        @NotNull String content,
        @NotNull String sender,
        @NotNull String recever) {
}
