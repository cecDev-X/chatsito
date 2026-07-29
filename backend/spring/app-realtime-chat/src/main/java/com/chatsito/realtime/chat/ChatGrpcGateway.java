package com.chatsito.realtime.chat;

import java.util.List;

public interface ChatGrpcGateway {
    List<String> getFriends(String userId);

    void sendMessage(String content, String sender, String receiver);
}
