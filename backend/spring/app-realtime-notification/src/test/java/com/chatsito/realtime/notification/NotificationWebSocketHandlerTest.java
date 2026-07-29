package com.chatsito.realtime.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

class NotificationWebSocketHandlerTest {
    private final Map<WebSocketSession, List<String>> messages = new HashMap<>();
    private final NotificationWebSocketHandler handler = new NotificationWebSocketHandler();

    @Test
    void routesExactJsonOnlyToTheConnectedMainuid() throws Exception {
        var recipient = session("recipient-session", "recipient-id");
        var other = session("other-session", "other-id");
        handler.afterConnectionEstablished(recipient);
        handler.afterConnectionEstablished(other);
        String json = "{\"_id\":\"notification-id\",\"deatils\":\"Legacy\"}";

        assertThat(handler.sendIfConnected("recipient-id", json)).isTrue();

        assertThat(messages.get(recipient)).containsExactly(json);
        assertThat(messages.get(other)).isEmpty();
        assertThat(handler.sendIfConnected("offline-id", json)).isFalse();
    }

    @Test
    void echoesIncomingTextExactly() throws Exception {
        var session = session("session-id", "user-id");
        handler.afterConnectionEstablished(session);

        handler.handleTextMessage(session, new TextMessage("plain frontend probe"));

        assertThat(messages.get(session)).containsExactly("plain frontend probe");
    }

    @Test
    void latestDuplicateConnectionSurvivesTheOldCloseCallback() throws Exception {
        var oldSession = session("old-session", "user-id");
        var newSession = session("new-session", "user-id");
        handler.afterConnectionEstablished(oldSession);

        handler.afterConnectionEstablished(newSession);
        handler.afterConnectionClosed(oldSession, CloseStatus.NORMAL);

        verify(oldSession).close(CloseStatus.NORMAL);
        assertThat(handler.hasConnection("user-id", newSession)).isTrue();
        assertThat(handler.sendIfConnected("user-id", "new-session-message")).isTrue();
        assertThat(messages.get(oldSession)).isEmpty();
        assertThat(messages.get(newSession)).containsExactly("new-session-message");
    }

    @Test
    void currentCloseRemovesTheDeliveryRoute() throws Exception {
        var session = session("session-id", "user-id");
        handler.afterConnectionEstablished(session);

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        assertThat(handler.hasConnection("user-id", session)).isFalse();
        assertThat(handler.sendIfConnected("user-id", "ignored")).isFalse();
    }

    private WebSocketSession session(String sessionId, String userId) throws Exception {
        var session = org.mockito.Mockito.mock(WebSocketSession.class);
        messages.put(session, new ArrayList<>());
        lenient().when(session.getId()).thenReturn(sessionId);
        lenient().when(session.getUri()).thenReturn(URI.create("ws://localhost:8088/ws/" + userId));
        lenient().when(session.isOpen()).thenReturn(true);
        lenient().doAnswer(invocation -> {
            messages.get(session).add(((TextMessage) invocation.getArgument(0)).getPayload());
            return null;
        }).when(session).sendMessage(any(TextMessage.class));
        return session;
    }
}
