package com.chatsito.api.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class PostUpdateServiceTest {
    private static final String POST_ID = "300000000000000000000001";
    private static final String OWNER_ID = "000000000000000000000001";

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void updatesOnlyEditableFieldsForOwner() {
        var original = post();
        when(mongoTemplate.findById(new ObjectId(POST_ID), PostDocument.class))
                .thenReturn(original);
        when(mongoTemplate.save(any(PostDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = new PostUpdateService(mongoTemplate).update(
                POST_ID, OWNER_ID, new CreatePostRequest("Updated message", "updated.png", "Updated title"));

        assertThat(result.status()).isEqualTo(PostUpdateResult.Status.SUCCESS);
        assertThat(result.post().title()).isEqualTo("Updated title");
        assertThat(result.post().creator()).isEqualTo(OWNER_ID);
        assertThat(result.post().likes()).containsExactly("liking-user");
        assertThat(result.post().createdAt()).isEqualTo(Instant.parse("2026-07-28T10:00:00Z"));
    }

    @Test
    void distinguishesMissingUnauthorizedAndMalformedPosts() {
        var service = new PostUpdateService(mongoTemplate);

        assertThat(service.update(POST_ID, OWNER_ID, request()).status())
                .isEqualTo(PostUpdateResult.Status.NOT_FOUND);

        when(mongoTemplate.findById(new ObjectId(POST_ID), PostDocument.class))
                .thenReturn(post());
        assertThat(service.update(POST_ID, "different-user", request()).status())
                .isEqualTo(PostUpdateResult.Status.NOT_AUTHORIZED);
        assertThat(service.update("not-an-object-id", OWNER_ID, request()).status())
                .isEqualTo(PostUpdateResult.Status.FAILED);
    }

    private CreatePostRequest request() {
        return new CreatePostRequest("message", "", "title");
    }

    private PostDocument post() {
        var post = new PostDocument();
        post.setId(new ObjectId(POST_ID));
        post.setTitle("Original title");
        post.setMessage("Original message");
        post.setCreator(OWNER_ID);
        post.setSelectedFile("");
        post.setLikes(List.of("liking-user"));
        post.setCreatedAt(Instant.parse("2026-07-28T10:00:00Z"));
        return post;
    }
}
