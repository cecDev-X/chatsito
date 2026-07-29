package com.chatsito.api.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chatsito.api.notification.NotificationCreationService;
import com.chatsito.api.user.UserDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {
    private static final String POST_ID = "300000000000000000000002";
    private static final String USER_ID = "000000000000000000000001";

    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private NotificationCreationService notificationCreationService;
    @Mock
    private PostDetailsService postDetailsService;

    @Test
    void storesCommentNotifiesCreatorAndReturnsPostDetails() {
        when(mongoTemplate.save(any(CommentDocument.class))).thenAnswer(invocation -> {
            CommentDocument comment = invocation.getArgument(0);
            comment.setId(new ObjectId("800000000000000000000001"));
            return comment;
        });
        var post = new PostDocument();
        post.setCreator("000000000000000000000004");
        var actor = new UserDocument();
        actor.setName("Spring Main");
        when(mongoTemplate.findById(new ObjectId(POST_ID), PostDocument.class)).thenReturn(post);
        when(mongoTemplate.findById(new ObjectId(USER_ID), UserDocument.class)).thenReturn(actor);
        var expected = new SinglePostResponse(null);
        when(postDetailsService.getPost(POST_ID)).thenReturn(expected);

        var response = new PostCommentService(
                mongoTemplate, notificationCreationService, postDetailsService)
                .create(POST_ID, USER_ID, "New comment");

        assertThat(response).isSameAs(expected);
        verify(notificationCreationService).create(
                "user Spring Main Comment On Your Post", post.getCreator(), POST_ID, actor);
        var order = inOrder(mongoTemplate);
        order.verify(mongoTemplate).save(any(CommentDocument.class));
        order.verify(mongoTemplate).findById(new ObjectId(POST_ID), PostDocument.class);
    }

    @Test
    void preservesLegacyOrphanCommentForMalformedPostId() {
        when(mongoTemplate.save(any(CommentDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new PostCommentService(
                mongoTemplate, notificationCreationService, postDetailsService);

        var response = service.create("not-an-object-id", USER_ID, "Orphan comment");

        assertThat(response).isNull();
        verify(mongoTemplate).save(any(CommentDocument.class));
        verify(notificationCreationService, never()).create(any(), any(), any(), any());
    }

    @Test
    void returnsNullWithoutWritingWhenValueIsMissing() {
        var response = new PostCommentService(
                mongoTemplate, notificationCreationService, postDetailsService)
                .create(POST_ID, USER_ID, null);

        assertThat(response).isNull();
        verify(mongoTemplate, never()).save(any(CommentDocument.class));
    }
}
