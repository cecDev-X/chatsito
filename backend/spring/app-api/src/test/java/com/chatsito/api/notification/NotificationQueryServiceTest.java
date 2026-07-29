package com.chatsito.api.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {
    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void returnsNotificationsWithNewestFirstQuery() {
        when(mongoTemplate.find(any(Query.class), eq(NotificationDocument.class)))
                .thenReturn(List.of(notification("000000000000000000000002", "newest")));

        var response = new NotificationQueryService(mongoTemplate)
                .getNotifications("main-user");

        var queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(NotificationDocument.class));
        assertThat(queryCaptor.getValue().getSortObject()).isEqualTo(new Document("_id", -1));
        assertThat(response.notifications()).extracting(NotificationResponse::details)
                .containsExactly("newest");
    }

    @Test
    void marksEveryExactRecipientNotificationRead() {
        new NotificationQueryService(mongoTemplate).markAllRead("main-user");

        verify(mongoTemplate).updateMulti(
                any(Query.class), any(Update.class), eq(NotificationDocument.class));
    }

    private NotificationDocument notification(String id, String details) {
        var user = new NotificationUserDocument();
        user.setName("Actor");
        user.setAvatar(null);

        var notification = new NotificationDocument();
        notification.setId(new ObjectId(id));
        notification.setDetails(details);
        notification.setMainuid("main-user");
        notification.setTargetid("target-id");
        notification.setRead(false);
        notification.setCreatedAt(Instant.parse("2026-07-28T12:00:00Z"));
        notification.setUser(user);
        return notification;
    }
}
