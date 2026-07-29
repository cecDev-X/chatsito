package com.chatsito.api.chat;

import java.util.List;

public record ChatHistoryResponse(List<ChatMessageResponse> msgs, boolean hasMore) {
}
