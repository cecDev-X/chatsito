package com.chatsito.api.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chatsito.api.user.UserDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class NotificationCreationServiceTest {
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private NotificationRealtimeClient realtimeClient;

    @Test
    void storesLegacyNotificationAndAttemptsRealtimeDelivery() {
        when(mongoTemplate.save(any(NotificationDocument.class))).thenAnswer(invocation -> {
            NotificationDocument notification = invocation.getArgument(0);
            notification.setId(new ObjectId("700000000000000000000001"));
            return notification;
        });
        var actor = new UserDocument();
        actor.setName("Spring Main");
        actor.setImageUrl("main.png");

        new NotificationCreationService(mongoTemplate, realtimeClient).create(
                "user Spring Main Like On Your Post", "recipient", "post-id", actor);

        var captor = ArgumentCaptor.forClass(NotificationDocument.class);
        verify(mongoTemplate).save(captor.capture());
        assertThat(captor.getValue().getDetails())
                .isEqualTo("user Spring Main Like On Your Post");
        assertThat(captor.getValue().getMainuid()).isEqualTo("recipient");
        assertThat(captor.getValue().isRead()).isFalse();
        verify(realtimeClient).send(captor.getValue());
    }
}
