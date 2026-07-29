package com.chatsito.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.Descriptors.FieldDescriptor;
import org.junit.jupiter.api.Test;

import com.chatsito.contracts.chat.ChatProto;
import com.chatsito.contracts.notification.NotificationProto;

class GrpcWireContractTest {
    @Test
    void preservesGrpcServiceNames() {
        var chatService = ChatProto.getDescriptor()
                .findServiceByName("RealTimeChatService");
        var notificationService = NotificationProto.getDescriptor()
                .findServiceByName("NotificationGrpcService");

        assertThat(chatService.getFullName()).isEqualTo("chat.RealTimeChatService");
        assertThat(notificationService.getFullName())
                .isEqualTo("Notification.NotificationGrpcService");
    }

    @Test
    void preservesTheCompleteChatWireContract() {
        var file = ChatProto.getDescriptor();
        var service = file.findServiceByName("RealTimeChatService");
        var sendMessage = service.findMethodByName("SendMessage");
        var friends = service.findMethodByName("GetUserFollowingFollowers");

        assertThat(service.getFullName()).isEqualTo("chat.RealTimeChatService");
        assertThat(sendMessage.isClientStreaming()).isFalse();
        assertThat(sendMessage.isServerStreaming()).isFalse();
        assertThat(sendMessage.getInputType().getFullName()).isEqualTo("chat.MessageRequest");
        assertThat(sendMessage.getOutputType().getFullName()).isEqualTo("chat.MessageResponse");
        assertThat(friends.isClientStreaming()).isFalse();
        assertThat(friends.isServerStreaming()).isFalse();
        assertThat(friends.getInputType().getFullName()).isEqualTo("chat.UserID");
        assertThat(friends.getOutputType().getFullName()).isEqualTo("chat.UsersIDsListResponse");

        assertField(file, "MessageRequest", "content", 1,
                FieldDescriptor.Type.STRING, false);
        assertField(file, "MessageRequest", "sender", 2,
                FieldDescriptor.Type.STRING, false);
        assertField(file, "MessageRequest", "receiver", 3,
                FieldDescriptor.Type.STRING, false);
        assertField(file, "MessageResponse", "message", 1,
                FieldDescriptor.Type.STRING, false);
        assertField(file, "UserID", "userid", 1,
                FieldDescriptor.Type.STRING, false);
        assertField(file, "UsersIDsListResponse", "userIDsLists", 1,
                FieldDescriptor.Type.MESSAGE, true);
        assertField(file, "UserIDsList", "userIdsList", 1,
                FieldDescriptor.Type.STRING, true);
    }

    @Test
    void preservesTheCompleteNotificationWireContract() {
        var file = NotificationProto.getDescriptor();
        var service = file.findServiceByName("NotificationGrpcService");
        var send = service.findMethodByName("SendGrpcNotification");

        assertThat(service.getFullName()).isEqualTo("Notification.NotificationGrpcService");
        assertThat(send.isClientStreaming()).isFalse();
        assertThat(send.isServerStreaming()).isFalse();
        assertThat(send.getInputType().getFullName())
                .isEqualTo("Notification.NotificationGrpcRequest");
        assertThat(send.getOutputType().getFullName()).isEqualTo("google.protobuf.Empty");

        assertField(file, "NotificationGrpcRequest", "_id", 1,
                FieldDescriptor.Type.STRING, false);
        assertThat(file.findMessageTypeByName("NotificationGrpcRequest")
                .findFieldByName("_id").getJsonName()).isEqualTo("Id");
        assertField(file, "NotificationGrpcRequest", "deatils", 2,
                FieldDescriptor.Type.STRING, false);
        assertField(file, "NotificationGrpcRequest", "mainuid", 3,
                FieldDescriptor.Type.STRING, false);
        assertField(file, "NotificationGrpcRequest", "targetid", 4,
                FieldDescriptor.Type.STRING, false);
        assertField(file, "NotificationGrpcRequest", "isreded", 5,
                FieldDescriptor.Type.BOOL, false);
        assertField(file, "NotificationGrpcRequest", "createdAt", 6,
                FieldDescriptor.Type.MESSAGE, false);
        assertField(file, "NotificationGrpcRequest", "user", 7,
                FieldDescriptor.Type.MESSAGE, false);
        assertThat(file.findMessageTypeByName("NotificationGrpcRequest")
                .findFieldByName("createdAt").getMessageType().getFullName())
                .isEqualTo("google.protobuf.Timestamp");
        assertThat(file.findMessageTypeByName("NotificationGrpcRequest")
                .findFieldByName("user").getMessageType().getFullName())
                .isEqualTo("Notification.Usergrpc");
        assertField(file, "Usergrpc", "name", 1, FieldDescriptor.Type.STRING, false);
        assertField(file, "Usergrpc", "avatar", 2, FieldDescriptor.Type.STRING, false);
    }

    private void assertField(
            com.google.protobuf.Descriptors.FileDescriptor file,
            String messageName,
            String fieldName,
            int number,
            FieldDescriptor.Type type,
            boolean repeated) {
        var field = file.findMessageTypeByName(messageName).findFieldByName(fieldName);
        assertThat(field.getNumber()).isEqualTo(number);
        assertThat(field.getType()).isEqualTo(type);
        assertThat(field.isRepeated()).isEqualTo(repeated);
    }
}
