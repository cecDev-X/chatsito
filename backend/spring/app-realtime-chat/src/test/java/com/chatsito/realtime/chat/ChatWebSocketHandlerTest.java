package com.chatsito.realtime.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketHandlerTest {
    @Mock
    private ChatGrpcGateway chatGrpcGateway;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<WebSocketSession, List<String>> messages = new HashMap<>();
    private ChatWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ChatWebSocketHandler(chatGrpcGateway, objectMapper);
    }

    @Test
    void connectionSendsFullPresenceSnapshotsOnlyWhenFriendsAreOnline() throws Exception {
        var friend = session("session-b", "B");
        var user = session("session-a", "A");
        when(chatGrpcGateway.getFriends("B")).thenReturn(List.of("A"));
        when(chatGrpcGateway.getFriends("A")).thenReturn(List.of("B", "B"));

        handler.afterConnectionEstablished(friend);
        assertThat(messages.get(friend)).isEmpty();

        handler.afterConnectionEstablished(user);

        assertOnlineSnapshot(user, "B");
        assertOnlineSnapshot(friend, "A");
    }

    @Test
    void requestOnlineTakesPrecedenceAndReturnsAnEmptySnapshot() throws Exception {
        var user = session("session-a", "A");
        when(chatGrpcGateway.getFriends("A")).thenReturn(List.of());
        handler.afterConnectionEstablished(user);

        handler.handleTextMessage(user, new TextMessage(
                "{\"type\":\"requestOnline\",\"sender\":\"A\",\"recever\":\"B\",\"content\":\"ignored\"}"));

        assertThat(messages.get(user)).hasSize(1);
        assertThat(objectMapper.readTree(messages.get(user).getFirst()).get("onlineFriends").isEmpty())
                .isTrue();
        verify(chatGrpcGateway, never()).sendMessage(any(), any(), any());
    }

    @Test
    void persistsBeforeDeliveringTheExactLegacyMessageWithoutSenderEcho() throws Exception {
        var events = new ArrayList<String>();
        var sender = session("session-a", "A");
        var receiver = session("session-b", "B", events);
        when(chatGrpcGateway.getFriends(any())).thenReturn(List.of());
        doAnswer(invocation -> {
            events.add("persist");
            return null;
        }).when(chatGrpcGateway).sendMessage("hello", "A", "B");
        handler.afterConnectionEstablished(sender);
        handler.afterConnectionEstablished(receiver);

        handler.handleTextMessage(sender,
                new TextMessage("{\"content\":\"hello\",\"sender\":\"A\",\"recever\":\"B\"}"));

        assertThat(events).containsExactly("persist", "deliver");
        assertThat(messages.get(sender)).isEmpty();
        assertThat(messages.get(receiver)).containsExactly(
                "{\"sender\":\"A\",\"recever\":\"B\",\"content\":\"hello\"}");
    }

    @Test
    void deliversRealtimeWhenPersistenceFailsAndPersistsWhenReceiverIsOffline() throws Exception {
        var sender = session("session-a", "A");
        var receiver = session("session-b", "B");
        when(chatGrpcGateway.getFriends(any())).thenReturn(List.of());
        doThrow(new IllegalStateException("gRPC unavailable"))
                .when(chatGrpcGateway).sendMessage("online", "A", "B");
        handler.afterConnectionEstablished(sender);
        handler.afterConnectionEstablished(receiver);

        handler.handleTextMessage(sender,
                new TextMessage("{\"sender\":\"A\",\"recever\":\"B\",\"content\":\"online\"}"));
        handler.handleTextMessage(sender,
                new TextMessage("{\"sender\":\"A\",\"recever\":\"C\",\"content\":\"offline\"}"));

        assertThat(messages.get(receiver)).containsExactly(
                "{\"sender\":\"A\",\"recever\":\"B\",\"content\":\"online\"}");
        verify(chatGrpcGateway).sendMessage("offline", "A", "C");
    }

    @Test
    void rejectsInvalidPayloadButAcceptsUnknownTypeAsACompleteMessage() throws Exception {
        var sender = session("session-a", "A");
        when(chatGrpcGateway.getFriends("A")).thenReturn(List.of());
        handler.afterConnectionEstablished(sender);

        handler.handleTextMessage(sender,
                new TextMessage("{\"type\":\"other\",\"sender\":\"A\",\"recever\":\"B\",\"content\":\"valid\"}"));
        verify(chatGrpcGateway).sendMessage("valid", "A", "B");

        handler.handleTextMessage(sender,
                new TextMessage("{\"sender\":\"A\",\"receiver\":\"B\",\"content\":\"invalid\"}"));

        verify(sender).close(CloseStatus.BAD_DATA);
        assertThat(handler.hasConnection("A", sender)).isFalse();
    }

    @Test
    void latestDuplicateConnectionCannotBeRemovedByTheOldCloseCallback() throws Exception {
        var oldSession = session("old", "A");
        var newSession = session("new", "A");
        when(chatGrpcGateway.getFriends("A")).thenReturn(List.of());

        handler.afterConnectionEstablished(oldSession);
        handler.afterConnectionEstablished(newSession);
        handler.afterConnectionClosed(oldSession, CloseStatus.NORMAL);

        verify(oldSession).close(CloseStatus.NORMAL);
        assertThat(handler.hasConnection("A", newSession)).isTrue();
    }

    @Test
    void disconnectRemovesTheUserBeforeBroadcastingTheEmptySnapshot() throws Exception {
        var user = session("session-a", "A");
        var friend = session("session-b", "B");
        when(chatGrpcGateway.getFriends("A")).thenReturn(List.of("B"));
        when(chatGrpcGateway.getFriends("B")).thenReturn(List.of("A"));
        handler.afterConnectionEstablished(friend);
        handler.afterConnectionEstablished(user);
        messages.get(friend).clear();

        handler.afterConnectionClosed(user, CloseStatus.NORMAL);

        assertThat(handler.hasConnection("A", user)).isFalse();
        assertThat(messages.get(friend)).hasSize(1);
        assertThat(objectMapper.readTree(messages.get(friend).getFirst()).get("onlineFriends").isEmpty())
                .isTrue();
    }

    private WebSocketSession session(String sessionId, String userId) throws Exception {
        return session(sessionId, userId, null);
    }

    private WebSocketSession session(String sessionId, String userId, List<String> events) throws Exception {
        var session = org.mockito.Mockito.mock(WebSocketSession.class);
        messages.put(session, new ArrayList<>());
        lenient().when(session.getId()).thenReturn(sessionId);
        lenient().when(session.getUri()).thenReturn(URI.create("ws://localhost:8001/ws/" + userId));
        lenient().when(session.isOpen()).thenReturn(true);
        lenient().doAnswer(invocation -> {
            messages.get(session).add(((TextMessage) invocation.getArgument(0)).getPayload());
            if (events != null) {
                events.add("deliver");
            }
            return null;
        }).when(session).sendMessage(any(TextMessage.class));
        return session;
    }

    private void assertOnlineSnapshot(WebSocketSession session, String expectedId) throws Exception {
        assertThat(messages.get(session)).hasSize(1);
        JsonNode online = objectMapper.readTree(messages.get(session).getFirst()).get("onlineFriends");
        assertThat(online).extracting(JsonNode::textValue).containsExactly(expectedId);
    }
}
