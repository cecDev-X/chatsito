package com.chatsito.api.chat.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.chatsito.api.chat.ChatSendService;
import com.chatsito.api.chat.SendMessageRequest;
import com.chatsito.api.user.UserDocument;
import com.chatsito.contracts.chat.MessageRequest;
import com.chatsito.contracts.chat.MessageResponse;
import com.chatsito.contracts.chat.UserID;
import com.chatsito.contracts.chat.UsersIDsListResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class ChatGrpcServiceTest {
    private static final String USER_ID = "000000000000000000000001";

    @Mock
    private ChatSendService chatSendService;
    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void translatesReceiverAndReturnsTheExactSuccessMessage() {
        var observer = new RecordingObserver<MessageResponse>();
        var request = MessageRequest.newBuilder()
                .setContent("hello")
                .setSender("sender-id")
                .setReceiver("receiver-id")
                .build();

        new ChatGrpcService(chatSendService, mongoTemplate).sendMessage(request, observer);

        var requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(chatSendService).send(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .isEqualTo(new SendMessageRequest("hello", "sender-id", "receiver-id"));
        assertThat(observer.value.getMessage()).isEqualTo("Message sent successfully");
        assertThat(observer.completed).isTrue();
        assertThat(observer.error).isNull();
    }

    @Test
    void reportsSuccessForEmptyFieldsAndPersistenceFailures() {
        when(chatSendService.send(any())).thenThrow(new IllegalStateException("Mongo failed"));
        var observer = new RecordingObserver<MessageResponse>();

        new ChatGrpcService(chatSendService, mongoTemplate)
                .sendMessage(MessageRequest.getDefaultInstance(), observer);

        var requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(chatSendService).send(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .isEqualTo(new SendMessageRequest("", "", ""));
        assertThat(observer.value.getMessage()).isEqualTo("Message sent successfully");
        assertThat(observer.completed).isTrue();
    }

    @Test
    void returnsOneNestedDeduplicatedFriendList() {
        var user = user(List.of("friend-a", "friend-b"), List.of("friend-b", "friend-c"));
        when(mongoTemplate.findById(new ObjectId(USER_ID), UserDocument.class)).thenReturn(user);
        var observer = new RecordingObserver<UsersIDsListResponse>();

        new ChatGrpcService(chatSendService, mongoTemplate)
                .getUserFollowingFollowers(UserID.newBuilder().setUserid(USER_ID).build(), observer);

        assertThat(observer.value.getUserIDsListsCount()).isEqualTo(1);
        assertThat(observer.value.getUserIDsLists(0).getUserIdsListList())
                .containsExactlyInAnyOrder("friend-a", "friend-b", "friend-c");
        assertThat(observer.completed).isTrue();
    }

    @Test
    void returnsOneEmptyInnerListForAUserWithoutFriends() {
        when(mongoTemplate.findById(new ObjectId(USER_ID), UserDocument.class))
                .thenReturn(user(List.of(), List.of()));
        var observer = new RecordingObserver<UsersIDsListResponse>();

        new ChatGrpcService(chatSendService, mongoTemplate)
                .getUserFollowingFollowers(UserID.newBuilder().setUserid(USER_ID).build(), observer);

        assertThat(observer.value.getUserIDsListsCount()).isEqualTo(1);
        assertThat(observer.value.getUserIDsLists(0).getUserIdsListList()).isEmpty();
        assertThat(observer.completed).isTrue();
    }

    @Test
    void mapsInvalidMissingAndMalformedUsersToNotFound() {
        var service = new ChatGrpcService(chatSendService, mongoTemplate);

        assertNotFound(service, "not-an-object-id");
        assertNotFound(service, USER_ID);

        when(mongoTemplate.findById(new ObjectId(USER_ID), UserDocument.class))
                .thenReturn(user(null, List.of()));
        assertNotFound(service, USER_ID);
    }

    private void assertNotFound(ChatGrpcService service, String userId) {
        var observer = new RecordingObserver<UsersIDsListResponse>();
        service.getUserFollowingFollowers(UserID.newBuilder().setUserid(userId).build(), observer);

        assertThat(observer.completed).isFalse();
        assertThat(observer.error).isInstanceOf(StatusRuntimeException.class);
        assertThat(Status.fromThrowable(observer.error).getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }

    private UserDocument user(List<String> following, List<String> followers) {
        var user = new UserDocument();
        user.setId(new ObjectId(USER_ID));
        user.setFollowing(following);
        user.setFollowers(followers);
        return user;
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
