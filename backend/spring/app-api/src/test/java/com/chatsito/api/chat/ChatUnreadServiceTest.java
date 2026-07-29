package com.chatsito.api.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ExtendWith(MockitoExtension.class)
class ChatUnreadServiceTest {
    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void returnsOnlyUnreadRecordsAndSumsTheirCounters() {
        when(mongoTemplate.find(any(Query.class), eq(UnreadMessageDocument.class)))
                .thenReturn(List.of(unread("sender-a", 3), unread("sender-b", 2)));

        var response = new ChatUnreadService(mongoTemplate).getUnread("main-user");

        assertThat(response.total()).isEqualTo(5);
        assertThat(response.messages()).extracting(UnreadMessageResponse::otherUserid)
                .containsExactly("sender-a", "sender-b");
    }

    @Test
    void marksTheFirstMatchingPairAsRead() {
        var existing = unread("sender-a", 3);
        when(mongoTemplate.findOne(any(Query.class), eq(UnreadMessageDocument.class)))
                .thenReturn(existing);

        var response = new ChatUnreadService(mongoTemplate)
                .markRead("main-user", "sender-a");

        assertThat(response.isMarked()).isTrue();
        verify(mongoTemplate).updateFirst(
                any(Query.class), any(Update.class), eq(UnreadMessageDocument.class));
    }

    @Test
    void reportsFalseWhenThePairDoesNotExist() {
        when(mongoTemplate.findOne(any(Query.class), eq(UnreadMessageDocument.class)))
                .thenReturn(null);

        var response = new ChatUnreadService(mongoTemplate)
                .markRead("main-user", "unknown-user");

        assertThat(response.isMarked()).isFalse();
    }

    private UnreadMessageDocument unread(String otherUserId, int count) {
        var message = new UnreadMessageDocument();
        message.setId(new ObjectId());
        message.setMainUserid("main-user");
        message.setOtherUserid(otherUserId);
        message.setNumOfUnreadedMessages(count);
        message.setRead(false);
        return message;
    }
}
