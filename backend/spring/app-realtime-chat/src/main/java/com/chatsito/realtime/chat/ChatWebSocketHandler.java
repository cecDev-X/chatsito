package com.chatsito.realtime.chat;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final int SEND_TIMEOUT_MILLIS = 10_000;
    private static final int SEND_BUFFER_BYTES = 64 * 1024;

    private final ChatGrpcGateway chatGrpcGateway;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ChatGrpcGateway chatGrpcGateway, ObjectMapper objectMapper) {
        this.chatGrpcGateway = chatGrpcGateway;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = userId(session);
        var connection = new Connection(
                session,
                new ConcurrentWebSocketSessionDecorator(
                        session, SEND_TIMEOUT_MILLIS, SEND_BUFFER_BYTES));
        var previous = connections.put(userId, connection);
        if (previous != null) {
            close(previous.session(), CloseStatus.NORMAL);
        }

        try {
            var onlineFriends = onlineFriends(chatGrpcGateway.getFriends(userId));
            if (!onlineFriends.isEmpty()) {
                sendOnlineFriends(connection, onlineFriends);
            }
            notifyOnlineFriends(onlineFriends);
        } catch (RuntimeException exception) {
            connections.remove(userId, connection);
            close(connection.session(), CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String userId = userId(session);
        JsonNode payload;
        try {
            payload = objectMapper.readTree(message.getPayload());
        } catch (JsonProcessingException exception) {
            closeInvalidConnection(userId, session);
            return;
        }

        if (payload == null || !payload.isObject()) {
            closeInvalidConnection(userId, session);
            return;
        }
        if (payload.path("type").isTextual()
                && "requestOnline".equals(payload.path("type").textValue())) {
            try {
                sendOnlineFriends(currentConnection(userId, session),
                        onlineFriends(chatGrpcGateway.getFriends(userId)));
            } catch (RuntimeException exception) {
                closeInvalidConnection(userId, session);
            }
            return;
        }

        var sender = payload.get("sender");
        var receiver = payload.get("recever");
        var content = payload.get("content");
        if (!isText(sender) || !isText(receiver) || !isText(content)) {
            closeInvalidConnection(userId, session);
            return;
        }

        var receiverConnection = connections.get(receiver.textValue());
        try {
            chatGrpcGateway.sendMessage(
                    content.textValue(), sender.textValue(), receiver.textValue());
        } catch (RuntimeException exception) {
            // Persistence remains best-effort; realtime delivery still proceeds.
        }
        if (receiverConnection != null) {
            send(receiverConnection, chatMessage(sender.textValue(), receiver.textValue(), content.textValue()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        disconnectAndNotify(userId(session), session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        disconnectAndNotify(userId(session), session);
        close(session, CloseStatus.SERVER_ERROR);
    }

    boolean hasConnection(String userId, WebSocketSession session) {
        var connection = connections.get(userId);
        return connection != null && connection.session().getId().equals(session.getId());
    }

    private void closeInvalidConnection(String userId, WebSocketSession session) {
        disconnectAndNotify(userId, session);
        close(session, CloseStatus.BAD_DATA);
    }

    private void disconnectAndNotify(String userId, WebSocketSession session) {
        var current = connections.get(userId);
        if (current == null || !current.session().getId().equals(session.getId())
                || !connections.remove(userId, current)) {
            return;
        }
        try {
            notifyOnlineFriends(new LinkedHashSet<>(chatGrpcGateway.getFriends(userId)));
        } catch (RuntimeException exception) {
            // A failed lookup leaves presence unavailable for this disconnect event.
        }
    }

    private void notifyOnlineFriends(Iterable<String> friendIds) {
        for (String friendId : friendIds) {
            var friendConnection = connections.get(friendId);
            if (friendConnection == null) {
                continue;
            }
            try {
                sendOnlineFriends(friendConnection,
                        onlineFriends(chatGrpcGateway.getFriends(friendId)));
            } catch (RuntimeException exception) {
                return;
            }
        }
    }

    private List<String> onlineFriends(List<String> friendIds) {
        return onlineFriends(new LinkedHashSet<>(friendIds));
    }

    private List<String> onlineFriends(Iterable<String> friendIds) {
        var online = new LinkedHashSet<String>();
        for (String friendId : friendIds) {
            if (connections.containsKey(friendId)) {
                online.add(friendId);
            }
        }
        return List.copyOf(online);
    }

    private void sendOnlineFriends(Connection connection, List<String> friendIds) {
        var payload = objectMapper.createObjectNode();
        payload.set("onlineFriends", objectMapper.valueToTree(friendIds));
        send(connection, payload);
    }

    private ObjectNode chatMessage(String sender, String receiver, String content) {
        var payload = objectMapper.createObjectNode();
        payload.put("sender", sender);
        payload.put("recever", receiver);
        payload.put("content", content);
        return payload;
    }

    private void send(Connection connection, JsonNode payload) {
        if (!connection.sendSession().isOpen()) {
            return;
        }
        try {
            connection.sendSession().sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (IOException | RuntimeException exception) {
            // Retain the connection until its close callback handles cleanup.
        }
    }

    private Connection currentConnection(String userId, WebSocketSession session) {
        var connection = connections.get(userId);
        if (connection == null || !connection.session().getId().equals(session.getId())) {
            throw new IllegalStateException("connection replaced");
        }
        return connection;
    }

    private String userId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            throw new IllegalStateException("WebSocket URI missing");
        }
        String path = uri.getPath();
        int marker = path.lastIndexOf("/ws/");
        if (marker < 0 || marker + 4 >= path.length()) {
            throw new IllegalStateException("WebSocket user ID missing");
        }
        return path.substring(marker + 4);
    }

    private boolean isText(JsonNode value) {
        return value != null && value.isTextual();
    }

    private void close(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (IOException exception) {
            // Connection cleanup is already complete.
        }
    }

    private record Connection(
            WebSocketSession session,
            WebSocketSession sendSession) {
    }
}
