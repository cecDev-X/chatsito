package com.chatsito.realtime.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import com.chatsito.contracts.notification.NotificationGrpcRequest;
import com.chatsito.contracts.notification.Usergrpc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationGrpcServiceTest {
    @Mock
    private NotificationWebSocketDelivery delivery;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void convertsEveryFieldToTheExactLegacyWebSocketJson() throws Exception {
        when(delivery.sendIfConnected(anyString(), anyString())).thenReturn(true);
        var instant = Instant.parse("2026-07-28T12:34:56.123456789Z");
        var request = NotificationGrpcRequest.newBuilder()
                .setId("notification-id")
                .setDeatils("user Actor Like On Your Post")
                .setMainuid("recipient-id")
                .setTargetid("post-id")
                .setIsreded(false)
                .setCreatedAt(Timestamp.newBuilder()
                        .setSeconds(instant.getEpochSecond())
                        .setNanos(instant.getNano()))
                .setUser(Usergrpc.newBuilder().setName("Actor").setAvatar("avatar.png"))
                .build();
        var observer = new RecordingObserver<Empty>();

        new NotificationGrpcService(objectMapper, delivery)
                .sendGrpcNotification(request, observer);

        var recipient = ArgumentCaptor.forClass(String.class);
        var json = ArgumentCaptor.forClass(String.class);
        verify(delivery).sendIfConnected(recipient.capture(), json.capture());
        assertThat(recipient.getValue()).isEqualTo("recipient-id");
        var payload = objectMapper.readTree(json.getValue());
        assertThat(payload.size()).isEqualTo(7);
        assertThat(payload.get("_id").textValue()).isEqualTo("notification-id");
        assertThat(payload.get("deatils").textValue()).isEqualTo("user Actor Like On Your Post");
        assertThat(payload.get("mainuid").textValue()).isEqualTo("recipient-id");
        assertThat(payload.get("targetid").textValue()).isEqualTo("post-id");
        assertThat(payload.get("isreded").booleanValue()).isFalse();
        assertThat(payload.get("createdAt").textValue())
                .isEqualTo("2026-07-28T12:34:56.123456789Z");
        assertThat(payload.get("user").get("name").textValue()).isEqualTo("Actor");
        assertThat(payload.get("user").get("avatar").textValue()).isEqualTo("avatar.png");
        assertThat(observer.value).isEqualTo(Empty.getDefaultInstance());
        assertThat(observer.completed).isTrue();
        assertThat(observer.error).isNull();
    }

    @Test
    void disconnectedAndDefaultRequestsStillCompleteSuccessfully() throws Exception {
        when(delivery.sendIfConnected(anyString(), anyString())).thenReturn(false);
        var observer = new RecordingObserver<Empty>();

        new NotificationGrpcService(objectMapper, delivery)
                .sendGrpcNotification(NotificationGrpcRequest.getDefaultInstance(), observer);

        var json = ArgumentCaptor.forClass(String.class);
        verify(delivery).sendIfConnected(org.mockito.ArgumentMatchers.eq(""), json.capture());
        var payload = objectMapper.readTree(json.getValue());
        assertThat(payload.get("_id").textValue()).isEmpty();
        assertThat(payload.get("deatils").textValue()).isEmpty();
        assertThat(payload.get("isreded").booleanValue()).isFalse();
        assertThat(payload.get("createdAt").textValue()).isEqualTo("1970-01-01T00:00:00Z");
        assertThat(payload.get("user").get("name").textValue()).isEmpty();
        assertThat(payload.get("user").get("avatar").textValue()).isEmpty();
        assertThat(observer.completed).isTrue();
    }

    @Test
    void deliveryFailuresMapToTheContractUnknownStatus() {
        when(delivery.sendIfConnected(anyString(), anyString()))
                .thenThrow(new IllegalStateException("socket failed"));
        var observer = new RecordingObserver<Empty>();

        new NotificationGrpcService(objectMapper, delivery)
                .sendGrpcNotification(NotificationGrpcRequest.getDefaultInstance(), observer);

        assertThat(observer.completed).isFalse();
        assertThat(observer.error).isInstanceOf(StatusRuntimeException.class);
        assertThat(Status.fromThrowable(observer.error).getCode()).isEqualTo(Status.Code.UNKNOWN);
    }

    @Test
    void invalidTimestampMapsToUnknownWithoutAttemptingDelivery() {
        var request = NotificationGrpcRequest.newBuilder()
                .setCreatedAt(Timestamp.newBuilder().setSeconds(253_402_300_800L))
                .build();
        var observer = new RecordingObserver<Empty>();

        new NotificationGrpcService(objectMapper, delivery)
                .sendGrpcNotification(request, observer);

        verify(delivery, never()).sendIfConnected(anyString(), anyString());
        assertThat(Status.fromThrowable(observer.error).getCode()).isEqualTo(Status.Code.UNKNOWN);
    }

    private static final class RecordingObserver<T> implements StreamObserver<T> {
        private T value;
        private Throwable error;
        private boolean completed;

        @Override
        public void onNext(T value) {
            this.value = value;
        }

        @Override
        public void onError(Throwable throwable) {
            error = throwable;
        }

        @Override
        public void onCompleted() {
            completed = true;
        }
    }
}
