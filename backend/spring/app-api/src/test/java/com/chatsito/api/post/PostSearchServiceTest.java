package com.chatsito.api.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
class PostSearchServiceTest {
    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void returnsEmptyEnvelopeWithoutQueryingMongoForEmptySearch() {
        var response = new PostSearchService(mongoTemplate).search("");

        assertThat(response.data().user()).isEmpty();
        assertThat(response.data().posts()).isEmpty();
        verify(mongoTemplate, never()).find(any(Query.class), eq(PostDocument.class));
    }

    @Test
    void returnsMatchingUsersAndPostsInLegacyEnvelope() {
        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class)))
                .thenReturn(List.of(post()));
        when(mongoTemplate.find(any(Query.class), eq(UserDocument.class)))
                .thenReturn(List.of(user()));

        var response = new PostSearchService(mongoTemplate).search("visible");

        assertThat(response.data().posts()).extracting(PostResponse::title)
                .containsExactly("Visible Post");
        assertThat(response.data().user()).extracting(user -> user.name())
                .containsExactly("Visible User");
    }

    private PostDocument post() {
        var post = new PostDocument();
        post.setId(new ObjectId("300000000000000000000001"));
        post.setTitle("Visible Post");
        post.setMessage("Search result");
        post.setCreator("000000000000000000000001");
        post.setSelectedFile("");
        post.setLikes(List.of());
        post.setCreatedAt(Instant.parse("2026-07-28T12:00:00Z"));
        return post;
    }

    private UserDocument user() {
        var user = new UserDocument();
        user.setId(new ObjectId("000000000000000000000004"));
        user.setName("Visible User");
        user.setEmail("visible@example.com");
        user.setPassword("test-hash");
        user.setBio("");
        user.setImageUrl(null);
        user.setFollowers(List.of());
        user.setFollowing(List.of());
        return user;
    }
}
