package com.chatsito.api.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import com.chatsito.api.user.UserDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class PostFeedServiceTest {
    private static final String MAIN_ID = "000000000000000000000001";
    private static final String FOLLOWING_ID = "000000000000000000000002";

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void filtersByFollowingAndReturnsLegacyPagination() {
        var mainUser = user(MAIN_ID, "Main User");
        mainUser.setFollowing(List.of(FOLLOWING_ID));
        when(mongoTemplate.findById(new ObjectId(MAIN_ID), UserDocument.class))
                .thenReturn(mainUser);
        when(mongoTemplate.findById(new ObjectId(FOLLOWING_ID), UserDocument.class))
                .thenReturn(user(FOLLOWING_ID, "Following User"));
        when(mongoTemplate.count(any(Query.class), eq(PostDocument.class))).thenReturn(7L);
        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class)))
                .thenReturn(List.of(post()));
        when(mongoTemplate.find(any(Query.class), eq(CommentDocument.class)))
                .thenReturn(List.of(comment()));

        var response = new PostFeedService(mongoTemplate).getFeed("1", MAIN_ID, null);

        assertThat(response.currentPage()).isEqualTo(1);
        assertThat(response.numberOfPages()).isEqualTo(2);
        assertThat(response.data()).extracting(FeedPostResponse::title)
                .containsExactly("Feed Post");
        assertThat(response.data().getFirst().name()).isEqualTo("Main User");
        assertThat(response.data().getFirst().comments().getFirst().user().name())
                .isEqualTo("Following User");

        var queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(queryCaptor.capture(), eq(PostDocument.class));
        assertThat(queryCaptor.getValue().getQueryObject().toJson())
                .isEqualTo("{'creator': {'$in': ['000000000000000000000002', '000000000000000000000001']}}"
                        .replace('\'', '"'));
    }

    @Test
    void defaultsInvalidPageToOne() {
        when(mongoTemplate.count(any(Query.class), eq(PostDocument.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class))).thenReturn(List.of());

        var response = new PostFeedService(mongoTemplate).getFeed("invalid", null, null);

        assertThat(response.currentPage()).isEqualTo(1);
    }

    @Test
    void profileFilterTakesPriorityOverUserFeed() {
        when(mongoTemplate.count(any(Query.class), eq(PostDocument.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(PostDocument.class))).thenReturn(List.of());

        var response = new PostFeedService(mongoTemplate)
                .getFeed("2", "not-an-object-id", FOLLOWING_ID);

        assertThat(response.currentPage()).isEqualTo(2);
        var queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(queryCaptor.capture(), eq(PostDocument.class));
        assertThat(queryCaptor.getValue().getQueryObject().toJson())
                .isEqualTo("{'creator': '000000000000000000000002'}".replace('\'', '"'));
    }

    @Test
    void returnsFallbackSignalForInvalidUserWithoutProfile() {
        var response = new PostFeedService(mongoTemplate)
                .getFeed("1", "not-an-object-id", null);

        assertThat(response).isNull();
    }

    private PostDocument post() {
        var post = new PostDocument();
        post.setId(new ObjectId("300000000000000000000001"));
        post.setTitle("Feed Post");
        post.setMessage("message");
        post.setCreator(MAIN_ID);
        post.setSelectedFile("");
        post.setLikes(List.of());
        post.setCreatedAt(Instant.parse("2026-07-28T12:00:00Z"));
        return post;
    }

    private CommentDocument comment() {
        var comment = new CommentDocument();
        comment.setId(new ObjectId("500000000000000000000001"));
        comment.setPostId("300000000000000000000001");
        comment.setUserId(FOLLOWING_ID);
        comment.setValue("Fixture comment");
        comment.setCreatedAt(Instant.parse("2026-07-28T13:00:00Z"));
        return comment;
    }

    private UserDocument user(String id, String name) {
        var user = new UserDocument();
        user.setId(new ObjectId(id));
        user.setName(name);
        user.setEmail("user@example.com");
        user.setPassword("hash");
        user.setBio("");
        user.setFollowers(List.of());
        user.setFollowing(List.of());
        return user;
    }
}
