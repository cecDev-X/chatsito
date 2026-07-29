package com.chatsito.realtime.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import com.chatsito.contracts.chat.MessageRequest;
import com.chatsito.contracts.chat.MessageResponse;
import com.chatsito.contracts.chat.RealTimeChatServiceGrpc;
import com.chatsito.contracts.chat.UserID;
import com.chatsito.contracts.chat.UserIDsList;
import com.chatsito.contracts.chat.UsersIDsListResponse;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

class GrpcChatGatewayTest {
    @Test
    void usesTheServiceMethodsAndMapsReceverToProtoReceiver() throws Exception {
        var capturedMessage = new AtomicReference<MessageRequest>();
        var capturedUser = new AtomicReference<UserID>();
        var service = new RealTimeChatServiceGrpc.RealTimeChatServiceImplBase() {
            @Override
            public void sendMessage(
                    MessageRequest request,
                    StreamObserver<MessageResponse> responseObserver) {
                capturedMessage.set(request);
                responseObserver.onNext(MessageResponse.newBuilder().setMessage("ignored").build());
                responseObserver.onCompleted();
            }

            @Override
            public void getUserFollowingFollowers(
                    UserID request,
                    StreamObserver<UsersIDsListResponse> responseObserver) {
                capturedUser.set(request);
                responseObserver.onNext(UsersIDsListResponse.newBuilder()
                        .addUserIDsLists(UserIDsList.newBuilder()
                                .addUserIdsList("friend-a")
                                .addUserIdsList("friend-b"))
                        .build());
                responseObserver.onCompleted();
            }
        };
        var server = NettyServerBuilder.forPort(0).addService(service).build().start();
        var gateway = new GrpcChatGateway("localhost", server.getPort());
        try {
            gateway.sendMessage("hello", "sender-id", "receiver-id");
            var friends = gateway.getFriends("user-id");

            assertThat(capturedMessage.get().getContent()).isEqualTo("hello");
            assertThat(capturedMessage.get().getSender()).isEqualTo("sender-id");
            assertThat(capturedMessage.get().getReceiver()).isEqualTo("receiver-id");
            assertThat(capturedUser.get().getUserid()).isEqualTo("user-id");
            assertThat(friends).containsExactly("friend-a", "friend-b");
        } finally {
            gateway.destroy();
            server.shutdownNow().awaitTermination();
        }
    }
}
