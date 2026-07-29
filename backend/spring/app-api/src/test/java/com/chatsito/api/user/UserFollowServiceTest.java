package com.chatsito.api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.chatsito.api.notification.NotificationCreationService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class UserFollowServiceTest {
    private static final String TARGET_ID = "000000000000000000000004";
    private static final String ACTOR_ID = "000000000000000000000001";

    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private NotificationCreationService notificationCreationService;

    @Test
    void followsSymmetricallyAndCreatesNotification() {
        var target = user(TARGET_ID, "Target User");
        var actor = user(ACTOR_ID, "Spring Main");
        when(mongoTemplate.findById(new ObjectId(TARGET_ID), UserDocument.class)).thenReturn(target);
        when(mongoTemplate.findById(new ObjectId(ACTOR_ID), UserDocument.class)).thenReturn(actor);
        when(mongoTemplate.save(any(UserDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = new UserFollowService(mongoTemplate, notificationCreationService)
                .toggle(TARGET_ID, ACTOR_ID);

        assertThat(response.updateduser1().followers()).containsExactly(ACTOR_ID);
        assertThat(response.updateduser2().following()).containsExactly(TARGET_ID);
        verify(notificationCreationService).create(
                "user Spring Main Start Following You", TARGET_ID, ACTOR_ID, actor);
    }

    @Test
    void unfollowsSymmetricallyWithoutNotification() {
        var target = user(TARGET_ID, "Target User");
        target.setFollowers(List.of(ACTOR_ID));
        var actor = user(ACTOR_ID, "Spring Main");
        actor.setFollowing(List.of(TARGET_ID));
        when(mongoTemplate.findById(new ObjectId(TARGET_ID), UserDocument.class)).thenReturn(target);
        when(mongoTemplate.findById(new ObjectId(ACTOR_ID), UserDocument.class)).thenReturn(actor);
        when(mongoTemplate.save(any(UserDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = new UserFollowService(mongoTemplate, notificationCreationService)
                .toggle(TARGET_ID, ACTOR_ID);

        assertThat(response.updateduser1().followers()).isEmpty();
        assertThat(response.updateduser2().following()).isEmpty();
        verify(notificationCreationService, never()).create(any(), any(), any(), any());
    }

    @Test
    void returnsNullForMissingOrMalformedUsers() {
        var service = new UserFollowService(mongoTemplate, notificationCreationService);
        assertThat(service.toggle(TARGET_ID, ACTOR_ID)).isNull();
        assertThat(service.toggle("not-an-object-id", ACTOR_ID)).isNull();
    }

    private UserDocument user(String id, String name) {
        var user = new UserDocument();
        user.setId(new ObjectId(id));
        user.setName(name);
        user.setEmail(name.replace(' ', '.') + "@example.com");
        user.setPassword("hash");
        user.setBio("");
        user.setFollowers(List.of());
        user.setFollowing(List.of());
        return user;
    }
}
