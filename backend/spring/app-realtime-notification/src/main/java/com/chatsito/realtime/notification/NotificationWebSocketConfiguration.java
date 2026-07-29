package com.chatsito.realtime.notification;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class NotificationWebSocketConfiguration implements WebSocketConfigurer {
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    public NotificationWebSocketConfiguration(
            NotificationWebSocketHandler notificationWebSocketHandler) {
        this.notificationWebSocketHandler = notificationWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationWebSocketHandler, "/ws/{user_id}")
                .setAllowedOriginPatterns("*");
    }
}
