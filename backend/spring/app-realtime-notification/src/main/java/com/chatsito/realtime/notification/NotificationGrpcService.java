package com.chatsito.realtime.notification;

import com.chatsito.contracts.notification.NotificationGrpcRequest;
import com.chatsito.contracts.notification.NotificationGrpcServiceGrpc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Empty;
import com.google.protobuf.util.Timestamps;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationGrpcService
        extends NotificationGrpcServiceGrpc.NotificationGrpcServiceImplBase {
    private final ObjectMapper objectMapper;
    private final NotificationWebSocketDelivery delivery;

    @Autowired
    public NotificationGrpcService(
            ObjectMapper objectMapper,
            ObjectProvider<NotificationWebSocketDelivery> deliveryProvider) {
        this(objectMapper, deliveryProvider.getIfAvailable(() -> (recipientId, json) -> false));
    }

    NotificationGrpcService(ObjectMapper objectMapper, NotificationWebSocketDelivery delivery) {
        this.objectMapper = objectMapper;
        this.delivery = delivery;
    }

    @Override
    public void sendGrpcNotification(
            NotificationGrpcRequest request,
            StreamObserver<Empty> responseObserver) {
        try {
            delivery.sendIfConnected(request.getMainuid(), legacyJson(request));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (RuntimeException | JsonProcessingException exception) {
            responseObserver.onError(Status.UNKNOWN.asRuntimeException());
        }
    }

    private String legacyJson(NotificationGrpcRequest request) throws JsonProcessingException {
        var payload = objectMapper.createObjectNode();
        payload.put("_id", request.getId());
        payload.put("deatils", request.getDeatils());
        payload.put("mainuid", request.getMainuid());
        payload.put("targetid", request.getTargetid());
        payload.put("isreded", request.getIsreded());
        payload.put("createdAt", Timestamps.toString(request.getCreatedAt()));
        var user = payload.putObject("user");
        user.put("name", request.getUser().getName());
        user.put("avatar", request.getUser().getAvatar());
        return objectMapper.writeValueAsString(payload);
    }
}
