package com.chatsito.api.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import com.chatsito.api.notification.NotificationCreationService;
import com.chatsito.api.user.UserDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {
    private static final String POST_ID = "300000000000000000000002";
    private static final String USER_ID = "000000000000000000000001";

    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private NotificationCreationService notificationCreationService;

    @Test
    void addsLikeAndCreatesLegacyNotification() {
        var post = post(List.of());
        var actor = user();
        when(mongoTemplate.findById(new ObjectId(POST_ID), PostDocument.class)).thenReturn(post);
        when(mongoTemplate.findById(new ObjectId(USER_ID), UserDocument.class)).thenReturn(actor);
        when(mongoTemplate.save(any(PostDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = new PostLikeService(mongoTemplate, notificationCreationService)
                .toggle(POST_ID, USER_ID);

        assertThat(response.likes()).containsExactly(USER_ID);
        verify(notificationCreationService).create(
                "user Spring Main Like On Your Post", post.getCreator(), POST_ID, actor);
    }

    @Test
    void removesLikeWithoutCreatingNotification() {
        when(mongoTemplate.findById(new ObjectId(POST_ID), PostDocument.class))
                .thenReturn(post(List.of(USER_ID)));
        when(mongoTemplate.save(any(PostDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = new PostLikeService(mongoTemplate, notificationCreationService)
                .toggle(POST_ID, USER_ID);

        assertThat(response.likes()).isEmpty();
        verify(notificationCreationService, never()).create(any(), any(), any(), any());
    }

    @Test
    void returnsNullForMissingMalformedOrUnknownActor() {
        var service = new PostLikeService(mongoTemplate, notificationCreationService);
        assertThat(service.toggle(POST_ID, USER_ID)).isNull();
        assertThat(service.toggle("not-an-object-id", USER_ID)).isNull();

        when(mongoTemplate.findById(new ObjectId(POST_ID), PostDocument.class))
                .thenReturn(post(List.of()));
        assertThat(service.toggle(POST_ID, USER_ID)).isNull();
    }

    private PostDocument post(List<String> likes) {
        var post = new PostDocument();
        post.setId(new ObjectId(POST_ID));
        post.setTitle("Post");
        post.setMessage("message");
        post.setCreator("000000000000000000000004");
        post.setSelectedFile("");
        post.setLikes(likes);
        post.setCreatedAt(Instant.parse("2026-07-28T11:00:00Z"));
        return post;
    }

    private UserDocument user() {
        var user = new UserDocument();
        user.setId(new ObjectId(USER_ID));
        user.setName("Spring Main");
        user.setImageUrl("main.png");
        return user;
    }
}
