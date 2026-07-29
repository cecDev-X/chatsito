package com.chatsito.api.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class PostCreationServiceTest {
    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void createsPostWithAuthenticatedUserAndLegacyDefaults() {
        when(mongoTemplate.save(any(PostDocument.class))).thenAnswer(invocation -> {
            PostDocument post = invocation.getArgument(0);
            post.setId(new ObjectId("600000000000000000000001"));
            return post;
        });
        var request = new CreatePostRequest("message", "image", "Created Post");

        var response = new PostCreationService(mongoTemplate)
                .create(request, "000000000000000000000001");

        assertThat(response.creator()).isEqualTo("000000000000000000000001");
        assertThat(response.likes()).isEmpty();
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void returnsNullWhenMongoWriteFails() {
        when(mongoTemplate.save(any(PostDocument.class)))
                .thenThrow(new IllegalStateException("write failed"));

        var response = new PostCreationService(mongoTemplate)
                .create(new CreatePostRequest("message", "", "title"), "user");

        assertThat(response).isNull();
    }
}
