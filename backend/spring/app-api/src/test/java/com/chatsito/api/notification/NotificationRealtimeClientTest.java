package com.chatsito.api.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import com.chatsito.contracts.notification.NotificationGrpcRequest;
import com.chatsito.contracts.notification.NotificationGrpcServiceGrpc;
import com.google.protobuf.Empty;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

class NotificationRealtimeClientTest {
    @Test
    void sendsTheSavedNotificationOverTheWireContract() throws Exception {
        var captured = new AtomicReference<NotificationGrpcRequest>();
        var service = new NotificationGrpcServiceGrpc.NotificationGrpcServiceImplBase() {
            @Override
            public void sendGrpcNotification(
                    NotificationGrpcRequest request,
                    StreamObserver<Empty> responseObserver) {
                captured.set(request);
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
            }
        };
        var server = NettyServerBuilder.forPort(0).addService(service).build().start();
        try {
            var instant = Instant.parse("2026-07-28T12:34:56.123456789Z");
            var notification = notification(instant);

            new NotificationRealtimeClient("localhost", server.getPort()).send(notification);

            var request = captured.get();
            assertThat(request.getId()).isEqualTo("700000000000000000000001");
            assertThat(request.getDeatils()).isEqualTo("Legacy deatils");
            assertThat(request.getMainuid()).isEqualTo("recipient-id");
            assertThat(request.getTargetid()).isEqualTo("post-id");
            assertThat(request.getIsreded()).isFalse();
            assertThat(request.getCreatedAt().getSeconds()).isEqualTo(instant.getEpochSecond());
            assertThat(request.getCreatedAt().getNanos()).isEqualTo(instant.getNano());
            assertThat(request.getUser().getName()).isEqualTo("Actor");
            assertThat(request.getUser().getAvatar()).isEmpty();
        } finally {
            server.shutdownNow().awaitTermination();
        }
    }

    private NotificationDocument notification(Instant instant) {
        var user = new NotificationUserDocument();
        user.setName("Actor");
        user.setAvatar(null);
        var notification = new NotificationDocument();
        notification.setId(new ObjectId("700000000000000000000001"));
        notification.setDetails("Legacy deatils");
        notification.setMainuid("recipient-id");
        notification.setTargetid("post-id");
        notification.setRead(false);
        notification.setCreatedAt(instant);
        notification.setUser(user);
        return notification;
    }
}
