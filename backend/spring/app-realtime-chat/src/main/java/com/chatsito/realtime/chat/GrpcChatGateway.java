package com.chatsito.realtime.chat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.chatsito.contracts.chat.MessageRequest;
import com.chatsito.contracts.chat.RealTimeChatServiceGrpc;
import com.chatsito.contracts.chat.UserID;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GrpcChatGateway implements ChatGrpcGateway, DisposableBean {
    private final ManagedChannel channel;
    private final RealTimeChatServiceGrpc.RealTimeChatServiceBlockingStub stub;

    public GrpcChatGateway(
            @Value("${legacy.chat-grpc.host:localhost}") String host,
            @Value("${legacy.chat-grpc.port:5001}") int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        stub = RealTimeChatServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public List<String> getFriends(String userId) {
        var response = stub.getUserFollowingFollowers(
                UserID.newBuilder().setUserid(userId).build());
        return response.getUserIDsLists(0).getUserIdsListList();
    }

    @Override
    public void sendMessage(String content, String sender, String receiver) {
        stub.sendMessage(MessageRequest.newBuilder()
                .setContent(content)
                .setSender(sender)
                .setReceiver(receiver)
                .build());
    }

    @Override
    public void destroy() throws InterruptedException {
        channel.shutdown();
        if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
            channel.shutdownNow();
        }
    }
}
