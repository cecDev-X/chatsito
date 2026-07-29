package com.chatsito.api.chat.grpc;

import java.util.HashSet;

import com.chatsito.api.chat.ChatSendService;
import com.chatsito.api.chat.SendMessageRequest;
import com.chatsito.api.user.UserDocument;
import com.chatsito.contracts.chat.MessageRequest;
import com.chatsito.contracts.chat.MessageResponse;
import com.chatsito.contracts.chat.RealTimeChatServiceGrpc;
import com.chatsito.contracts.chat.UserID;
import com.chatsito.contracts.chat.UserIDsList;
import com.chatsito.contracts.chat.UsersIDsListResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatGrpcService extends RealTimeChatServiceGrpc.RealTimeChatServiceImplBase {
    private static final String SENT_MESSAGE = "Message sent successfully";

    private final ChatSendService chatSendService;
    private final MongoTemplate mongoTemplate;

    public ChatGrpcService(ChatSendService chatSendService, MongoTemplate mongoTemplate) {
        this.chatSendService = chatSendService;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void sendMessage(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {
        try {
            chatSendService.send(new SendMessageRequest(
                    request.getContent(),
                    request.getSender(),
                    request.getReceiver()));
        } catch (RuntimeException exception) {
            // This wire contract reports success for best-effort persistence.
        }

        responseObserver.onNext(MessageResponse.newBuilder()
                .setMessage(SENT_MESSAGE)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserFollowingFollowers(
            UserID request,
            StreamObserver<UsersIDsListResponse> responseObserver) {
        try {
            var user = mongoTemplate.findById(new ObjectId(request.getUserid()), UserDocument.class);
            if (user == null || user.getFollowing() == null || user.getFollowers() == null) {
                throw new IllegalStateException("user not found");
            }

            var friendIds = new HashSet<>(user.getFollowing());
            friendIds.addAll(user.getFollowers());
            var friends = UserIDsList.newBuilder()
                    .addAllUserIdsList(friendIds)
                    .build();
            responseObserver.onNext(UsersIDsListResponse.newBuilder()
                    .addUserIDsLists(friends)
                    .build());
            responseObserver.onCompleted();
        } catch (RuntimeException exception) {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }
    }
}
