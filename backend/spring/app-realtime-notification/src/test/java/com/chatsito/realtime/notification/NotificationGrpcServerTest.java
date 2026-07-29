package com.chatsito.realtime.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.chatsito.contracts.notification.NotificationGrpcRequest;
import com.chatsito.contracts.notification.NotificationGrpcServiceGrpc;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;

class NotificationGrpcServerTest {
    @Test
    void servesTheGeneratedContractOverPlaintextAndStopsCleanly() throws Exception {
        var deliveredJson = new AtomicReference<String>();
        NotificationWebSocketDelivery delivery = (recipientId, json) -> {
            deliveredJson.set(json);
            return true;
        };
        var service = new NotificationGrpcService(new ObjectMapper(), delivery);
        var server = new NotificationGrpcServer(0, service);

        server.start();
        var channel = ManagedChannelBuilder.forAddress("localhost", server.boundPort())
                .usePlaintext()
                .build();
        try {
            NotificationGrpcServiceGrpc.newBlockingStub(channel)
                    .sendGrpcNotification(NotificationGrpcRequest.newBuilder()
                            .setId("transport-id")
                            .setMainuid("recipient-id")
                            .build());

            assertThat(deliveredJson.get()).contains(
                    "\"_id\":\"transport-id\"",
                    "\"mainuid\":\"recipient-id\"");
            assertThat(server.isRunning()).isTrue();
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            server.stop();
        }

        assertThat(server.isRunning()).isFalse();
        assertThat(server.boundPort()).isEqualTo(-1);
    }
}
