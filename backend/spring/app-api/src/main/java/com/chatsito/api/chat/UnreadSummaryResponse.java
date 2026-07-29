package com.chatsito.api.chat;

import java.util.List;

public record UnreadSummaryResponse(List<UnreadMessageResponse> messages, int total) {
}
