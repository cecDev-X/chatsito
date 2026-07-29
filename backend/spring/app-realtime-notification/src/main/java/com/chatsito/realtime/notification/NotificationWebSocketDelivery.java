package com.chatsito.realtime.notification;

@FunctionalInterface
public interface NotificationWebSocketDelivery {
    boolean sendIfConnected(String recipientId, String json);
}
