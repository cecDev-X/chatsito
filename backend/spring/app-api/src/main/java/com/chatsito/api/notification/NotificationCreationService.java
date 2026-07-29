package com.chatsito.api.notification;

import java.time.Instant;

import com.chatsito.api.user.UserDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationCreationService {
    private final MongoTemplate mongoTemplate;
    private final NotificationRealtimeClient realtimeClient;

    public NotificationCreationService(
            MongoTemplate mongoTemplate,
            NotificationRealtimeClient realtimeClient) {
        this.mongoTemplate = mongoTemplate;
        this.realtimeClient = realtimeClient;
    }

    public void create(String details, String recipientId, String targetId, UserDocument actor) {
        try {
            var user = new NotificationUserDocument();
            user.setName(actor.getName());
            user.setAvatar(actor.getImageUrl());

            var notification = new NotificationDocument();
            notification.setDetails(details);
            notification.setMainuid(recipientId);
            notification.setTargetid(targetId);
            notification.setRead(false);
            notification.setCreatedAt(Instant.now());
            notification.setUser(user);

            var saved = mongoTemplate.save(notification);
            realtimeClient.send(saved);
        } catch (RuntimeException exception) {
            // Notification persistence and delivery must not fail the originating action.
        }
    }
}
