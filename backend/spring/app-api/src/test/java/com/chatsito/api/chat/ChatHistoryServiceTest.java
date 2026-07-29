package com.chatsito.api.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class ChatHistoryServiceTest {
    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void returnsEachPageInChronologicalOrderWithLegacyPagination() {
        var older = message("000000000000000000000001", "first");
        var newer = message("000000000000000000000002", "second");
        when(mongoTemplate.count(any(Query.class), eq(MessageDocument.class))).thenReturn(9L);
        when(mongoTemplate.find(any(Query.class), eq(MessageDocument.class)))
                .thenReturn(new ArrayList<>(List.of(newer, older)));

        var response = new ChatHistoryService(mongoTemplate)
                .getHistory(0, "sender-id", "receiver-id");

        assertThat(response.msgs()).extracting(ChatMessageResponse::content)
                .containsExactly("first", "second");
        assertThat(response.hasMore()).isTrue();
    }

    private MessageDocument message(String id, String content) {
        var message = new MessageDocument();
        message.setId(new ObjectId(id));
        message.setContent(content);
        message.setSender("sender-id");
        message.setReceiver("receiver-id");
        return message;
    }
}
