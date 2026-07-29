package com.chatsito.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UnreadMessageResponse(
        String id,
        String mainUserid,
        String otherUserid,
        int numOfUnreadedMessages,
        @JsonProperty("isReaded") boolean read) {
    static UnreadMessageResponse from(UnreadMessageDocument message) {
        return new UnreadMessageResponse(
                message.getId().toHexString(),
                message.getMainUserid(),
                message.getOtherUserid(),
                message.getNumOfUnreadedMessages(),
                message.isRead());
    }
}
