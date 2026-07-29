package com.chatsito.realtime.notification;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler
        implements NotificationWebSocketDelivery {
    private static final int SEND_TIMEOUT_MILLIS = 10_000;
    private static final int SEND_BUFFER_BYTES = 64 * 1024;

    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = userId(session);
        var connection = new Connection(
                session,
                new ConcurrentWebSocketSessionDecorator(
                        session, SEND_TIMEOUT_MILLIS, SEND_BUFFER_BYTES));
        var previous = connections.put(userId, connection);
        if (previous != null) {
            close(previous.session(), CloseStatus.NORMAL);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        var connection = connections.get(userId(session));
        if (connection != null && connection.session().getId().equals(session.getId())) {
            connection.sendSession().sendMessage(new TextMessage(message.getPayload()));
        }
    }

    @Override
    public boolean sendIfConnected(String recipientId, String json) {
        var connection = connections.get(recipientId);
        if (connection == null) {
            return false;
        }
        try {
            connection.sendSession().sendMessage(new TextMessage(json));
            return true;
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Unable to deliver realtime notification", exception);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeIfCurrent(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        removeIfCurrent(session);
        close(session, CloseStatus.SERVER_ERROR);
    }

    boolean hasConnection(String userId, WebSocketSession session) {
        var connection = connections.get(userId);
        return connection != null && connection.session().getId().equals(session.getId());
    }

    private void removeIfCurrent(WebSocketSession session) {
        String userId = userId(session);
        var connection = connections.get(userId);
        if (connection != null && connection.session().getId().equals(session.getId())) {
            connections.remove(userId, connection);
        }
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

    private void close(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (IOException exception) {
            // The map already points at the replacement session.
        }
    }

    private record Connection(
            WebSocketSession session,
            WebSocketSession sendSession) {
    }
}
