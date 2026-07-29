package com.chatsito.api.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class PostDeleteServiceTest {
    private static final String POST_ID = "300000000000000000000007";
    private static final String OWNER_ID = "000000000000000000000004";

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void deletesPostForOwner() {
        var post = post();
        when(mongoTemplate.findById(new ObjectId(POST_ID), PostDocument.class)).thenReturn(post);
        when(mongoTemplate.remove(post)).thenReturn(DeleteResult.acknowledged(1));

        var result = new PostDeleteService(mongoTemplate).delete(POST_ID, OWNER_ID);

        assertThat(result).isEqualTo(PostDeleteResult.SUCCESS);
        verify(mongoTemplate).remove(post);
    }

    @Test
    void distinguishesMissingUnauthorizedAndMalformedPosts() {
        var service = new PostDeleteService(mongoTemplate);
        assertThat(service.delete(POST_ID, OWNER_ID)).isEqualTo(PostDeleteResult.NOT_FOUND);

        var post = post();
        when(mongoTemplate.findById(new ObjectId(POST_ID), PostDocument.class)).thenReturn(post);
        assertThat(service.delete(POST_ID, "different-user"))
                .isEqualTo(PostDeleteResult.NOT_AUTHORIZED);
        verify(mongoTemplate, never()).remove(post);

        assertThat(service.delete("not-an-object-id", OWNER_ID))
                .isEqualTo(PostDeleteResult.FAILED);
    }

    private PostDocument post() {
        var post = new PostDocument();
        post.setId(new ObjectId(POST_ID));
        post.setCreator(OWNER_ID);
        return post;
    }
}
