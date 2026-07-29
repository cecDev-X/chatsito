package com.chatsito.api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {
    private static final String MAIN_ID = "000000000000000000000001";
    private static final String FOLLOWING_ID = "000000000000000000000002";
    private static final String FOLLOWER_ID = "000000000000000000000003";

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void returnsTheLegacyProfileEnvelope() {
        var user = user(MAIN_ID, "Main User");
        when(mongoTemplate.findById(new ObjectId(MAIN_ID), UserDocument.class)).thenReturn(user);

        var response = new UserQueryService(mongoTemplate).getProfile(MAIN_ID);

        assertThat(response.posts()).isEqualTo("posts");
        assertThat(response.user().id()).isEqualTo(MAIN_ID);
        assertThat(response.user().password()).isEqualTo("hashed-password");
    }

    @Test
    void excludesExistingRelationshipsAndLimitsSuggestions() {
        var main = user(MAIN_ID, "Main User");
        main.setFollowing(List.of(FOLLOWING_ID));
        main.setFollowers(List.of(FOLLOWER_ID));
        var suggested = user("000000000000000000000004", "Suggested User");
        when(mongoTemplate.findById(new ObjectId(MAIN_ID), UserDocument.class)).thenReturn(main);
        when(mongoTemplate.find(any(Query.class), eq(UserDocument.class)))
                .thenReturn(List.of(suggested));

        var response = new UserQueryService(mongoTemplate).getSuggestions(MAIN_ID);

        assertThat(response.users()).extracting(UserResponse::name)
                .containsExactly("Suggested User");
    }

    @Test
    void returnsNoSuggestionsWhenMainUserDoesNotExist() {
        when(mongoTemplate.findById(new ObjectId(MAIN_ID), UserDocument.class)).thenReturn(null);

        var response = new UserQueryService(mongoTemplate).getSuggestions(MAIN_ID);

        assertThat(response.users()).isEmpty();
    }

    private UserDocument user(String id, String name) {
        var user = new UserDocument();
        user.setId(new ObjectId(id));
        user.setName(name);
        user.setEmail(name.toLowerCase().replace(' ', '.') + "@example.com");
        user.setPassword("hashed-password");
        user.setBio("");
        user.setImageUrl(null);
        user.setFollowers(List.of());
        user.setFollowing(List.of());
        return user;
    }
}
