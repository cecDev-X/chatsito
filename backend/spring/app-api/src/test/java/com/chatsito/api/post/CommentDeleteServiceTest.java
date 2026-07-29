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
class CommentDeleteServiceTest {
    private static final String COMMENT_ID = "500000000000000000000001";

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void deletesExistingComment() {
        var comment = new CommentDocument();
        comment.setId(new ObjectId(COMMENT_ID));
        when(mongoTemplate.findById(new ObjectId(COMMENT_ID), CommentDocument.class))
                .thenReturn(comment);
        when(mongoTemplate.remove(comment)).thenReturn(DeleteResult.acknowledged(1));

        assertThat(new CommentDeleteService(mongoTemplate).delete(COMMENT_ID)).isTrue();
        verify(mongoTemplate).remove(comment);
    }

    @Test
    void returnsFalseForMissingAndMalformedIds() {
        var service = new CommentDeleteService(mongoTemplate);

        assertThat(service.delete(COMMENT_ID)).isFalse();
        assertThat(service.delete("not-an-object-id")).isFalse();
        verify(mongoTemplate, never()).remove(org.mockito.ArgumentMatchers.any(CommentDocument.class));
    }
}
