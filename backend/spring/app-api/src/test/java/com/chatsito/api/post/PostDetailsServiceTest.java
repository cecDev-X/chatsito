package com.chatsito.api.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import com.chatsito.api.user.UserDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class PostDetailsServiceTest {
    private static final String POST_ID = "300000000000000000000007";
    private static final String USER_ID = "000000000000000000000002";

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void returnsPostWithEnrichedComments() {
        when(mongoTemplate.findById(new ObjectId(POST_ID), PostDocument.class))
                .thenReturn(post());
        when(mongoTemplate.find(any(Query.class), eq(CommentDocument.class)))
                .thenReturn(List.of(comment()));
        when(mongoTemplate.findById(new ObjectId(USER_ID), UserDocument.class))
                .thenReturn(user());

        var response = new PostDetailsService(mongoTemplate).getPost(POST_ID);

        assertThat(response.post().id()).isEqualTo(POST_ID);
        assertThat(response.post().comments()).hasSize(1);
        assertThat(response.post().comments().getFirst().user().name())
                .isEqualTo("Comment User");
    }

    @Test
    void returnsNullForMissingOrInvalidPost() {
        var service = new PostDetailsService(mongoTemplate);

        assertThat(service.getPost("not-an-object-id")).isNull();
        assertThat(service.getPost(POST_ID)).isNull();
    }

    private PostDocument post() {
        var post = new PostDocument();
        post.setId(new ObjectId(POST_ID));
        post.setTitle("Details Post");
        post.setMessage("message");
        post.setCreator(USER_ID);
        post.setSelectedFile("");
        post.setLikes(List.of());
        post.setCreatedAt(Instant.parse("2026-07-28T16:00:00Z"));
        return post;
    }

    private CommentDocument comment() {
        var comment = new CommentDocument();
        comment.setId(new ObjectId("500000000000000000000001"));
        comment.setPostId(POST_ID);
        comment.setUserId(USER_ID);
        comment.setValue("Fixture comment");
        comment.setCreatedAt(Instant.parse("2026-07-28T17:00:00Z"));
        return comment;
    }

    private UserDocument user() {
        var user = new UserDocument();
        user.setId(new ObjectId(USER_ID));
        user.setName("Comment User");
        user.setImageUrl("comment-user.png");
        return user;
    }
}
