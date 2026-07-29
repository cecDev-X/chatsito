package com.chatsito.api.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ExtendWith(MockitoExtension.class)
class ChatSendServiceTest {
    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void savesLegacyMessageAndUpsertsReceiverUnreadCounter() {
        when(mongoTemplate.save(any(MessageDocument.class))).thenAnswer(invocation -> {
            MessageDocument message = invocation.getArgument(0);
            message.setId(new ObjectId("900000000000000000000001"));
            return message;
        });
        var request = new SendMessageRequest("hello", "sender-id", "receiver-id");

        var response = new ChatSendService(mongoTemplate).send(request);

        assertThat(response.msg().content()).isEqualTo("hello");
        assertThat(response.msg().sender()).isEqualTo("sender-id");
        assertThat(response.msg().receiver()).isEqualTo("receiver-id");
        var messageCaptor = ArgumentCaptor.forClass(MessageDocument.class);
        verify(mongoTemplate).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("hello");
        assertThat(messageCaptor.getValue().getSender()).isEqualTo("sender-id");
        assertThat(messageCaptor.getValue().getReceiver()).isEqualTo("receiver-id");
        var queryCaptor = ArgumentCaptor.forClass(Query.class);
        var updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).findAndModify(
                queryCaptor.capture(),
                updateCaptor.capture(),
                any(FindAndModifyOptions.class),
                org.mockito.ArgumentMatchers.eq(UnreadMessageDocument.class));
        assertThat(queryCaptor.getValue().getQueryObject().toJson())
                .contains("mainUserid", "receiver-id", "otherUserid", "sender-id");
        assertThat(updateCaptor.getValue().getUpdateObject().toJson())
                .contains("numOfUnreadedMessages", "isReaded");
    }

    @Test
    void returnsSavedMessageWhenUnreadUpdateFails() {
        when(mongoTemplate.save(any(MessageDocument.class))).thenAnswer(invocation -> {
            MessageDocument message = invocation.getArgument(0);
            message.setId(new ObjectId("900000000000000000000001"));
            return message;
        });
        when(mongoTemplate.findAndModify(
                any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
                org.mockito.ArgumentMatchers.eq(UnreadMessageDocument.class)))
                .thenThrow(new IllegalStateException("unread failed"));

        var response = new ChatSendService(mongoTemplate)
                .send(new SendMessageRequest("hello", "sender", "receiver"));

        assertThat(response.msg().id()).isEqualTo("900000000000000000000001");
    }

    @Test
    void returnsNullWhenMessageWriteFails() {
        when(mongoTemplate.save(any(MessageDocument.class)))
                .thenThrow(new IllegalStateException("message failed"));

        var response = new ChatSendService(mongoTemplate)
                .send(new SendMessageRequest("hello", "sender", "receiver"));

        assertThat(response).isNull();
    }
}
