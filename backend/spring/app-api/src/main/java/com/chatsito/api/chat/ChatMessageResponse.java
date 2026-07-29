package com.chatsito.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatMessageResponse(
        @JsonProperty("_id") String id,
        String content,
        String sender,
        @JsonProperty("recever") String receiver) {
    static ChatMessageResponse from(MessageDocument message) {
        return new ChatMessageResponse(
                message.getId().toHexString(),
                message.getContent(),
                message.getSender(),
                message.getReceiver());
    }
}
