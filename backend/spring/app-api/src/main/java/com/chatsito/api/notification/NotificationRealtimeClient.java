package com.chatsito.api.notification;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import com.chatsito.contracts.notification.NotificationGrpcRequest;
import com.chatsito.contracts.notification.NotificationGrpcServiceGrpc;
import com.chatsito.contracts.notification.Usergrpc;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationRealtimeClient {
    private final String host;
    private final int port;

    public NotificationRealtimeClient(
            @Value("${legacy.notification-grpc.host:localhost}") String host,
            @Value("${legacy.notification-grpc.port:8090}") int port) {
        this.host = host;
        this.port = port;
    }

    public void send(NotificationDocument notification) {
        var channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        try {
            var createdAt = timestamp(notification.getCreatedAt());
            var user = notification.getUser();
            var request = NotificationGrpcRequest.newBuilder()
                    .setId(notification.getId().toHexString())
                    .setDeatils(notification.getDetails())
                    .setMainuid(notification.getMainuid())
                    .setTargetid(notification.getTargetid())
                    .setIsreded(notification.isRead())
                    .setCreatedAt(createdAt)
                    .setUser(Usergrpc.newBuilder()
                            .setName(user.getName())
                            .setAvatar(user.getAvatar() == null ? "" : user.getAvatar())
                            .build())
                    .build();
            NotificationGrpcServiceGrpc.newBlockingStub(channel)
                    .withDeadlineAfter(1, TimeUnit.SECONDS)
                    .sendGrpcNotification(request);
        } catch (RuntimeException exception) {
            // Realtime delivery is best-effort; MongoDB remains the notification source of truth.
        } finally {
            channel.shutdownNow();
        }
    }

    private Timestamp timestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
