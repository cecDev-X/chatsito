package com.chatsito.api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class UserUpdateServiceTest {
    private static final String USER_ID = "000000000000000000000001";

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void updatesOnlyEditableProfileFields() {
        var user = user();
        when(mongoTemplate.findById(new ObjectId(USER_ID), UserDocument.class)).thenReturn(user);
        when(mongoTemplate.save(any(UserDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = new UserUpdateService(mongoTemplate).update(
                USER_ID, new UpdateUserRequest("Updated Name", "Updated bio", "updated.png"));

        assertThat(response.user().name()).isEqualTo("Updated Name");
        assertThat(response.user().email()).isEqualTo("main@spring.test");
        assertThat(response.user().password()).isEqualTo("bcrypt-hash");
        assertThat(response.user().following()).containsExactly("following-id");
        assertThat(response.posts()).isEqualTo("posts");
    }

    @Test
    void returnsNullForMissingOrMalformedUser() {
        var service = new UserUpdateService(mongoTemplate);
        var request = new UpdateUserRequest("Name", "bio", "image.png");

        assertThat(service.update(USER_ID, request)).isNull();
        assertThat(service.update("not-an-object-id", request)).isNull();
    }

    private UserDocument user() {
        var user = new UserDocument();
        user.setId(new ObjectId(USER_ID));
        user.setName("Spring Main");
        user.setEmail("main@spring.test");
        user.setPassword("bcrypt-hash");
        user.setBio("bio");
        user.setImageUrl("main.png");
        user.setFollowers(List.of("follower-id"));
        user.setFollowing(List.of("following-id"));
        return user;
    }
}
