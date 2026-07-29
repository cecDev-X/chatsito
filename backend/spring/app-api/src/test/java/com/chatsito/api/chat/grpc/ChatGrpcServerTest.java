package com.chatsito.api.chat.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.chatsito.api.chat.ChatSendService;
import com.chatsito.api.user.UserDocument;
import com.chatsito.contracts.chat.MessageRequest;
import com.chatsito.contracts.chat.RealTimeChatServiceGrpc;
import com.chatsito.contracts.chat.UserID;
import io.grpc.ManagedChannelBuilder;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

class ChatGrpcServerTest {
    private static final String USER_ID = "000000000000000000000001";

    @Test
    void servesTheGeneratedContractOverAPlaintextSocketAndStopsCleanly() throws Exception {
        var chatSendService = org.mockito.Mockito.mock(ChatSendService.class);
        var mongoTemplate = org.mockito.Mockito.mock(MongoTemplate.class);
        var user = new UserDocument();
        user.setId(new ObjectId(USER_ID));
        user.setFollowing(List.of("friend-a"));
        user.setFollowers(List.of("friend-b"));
        when(mongoTemplate.findById(new ObjectId(USER_ID), UserDocument.class)).thenReturn(user);
        var server = new ChatGrpcServer(0, new ChatGrpcService(chatSendService, mongoTemplate));

        server.start();
        var channel = ManagedChannelBuilder.forAddress("localhost", server.boundPort())
                .usePlaintext()
                .build();
        try {
            var stub = RealTimeChatServiceGrpc.newBlockingStub(channel);
            var sent = stub.sendMessage(MessageRequest.newBuilder()
                    .setContent("transport test")
                    .setSender("sender")
                    .setReceiver("receiver")
                    .build());
            var friends = stub.getUserFollowingFollowers(
                    UserID.newBuilder().setUserid(USER_ID).build());

            assertThat(sent.getMessage()).isEqualTo("Message sent successfully");
            assertThat(friends.getUserIDsListsCount()).isEqualTo(1);
            assertThat(friends.getUserIDsLists(0).getUserIdsListList())
                    .containsExactlyInAnyOrder("friend-a", "friend-b");
            assertThat(server.isRunning()).isTrue();
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            server.stop();
        }

        assertThat(server.isRunning()).isFalse();
        assertThat(server.boundPort()).isEqualTo(-1);
    }
}
