package com.chatsito.api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class UserDeleteServiceTest {
    private static final String USER_ID = "000000000000000000000004";

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    void deletesExistingUser() {
        var user = new UserDocument();
        user.setId(new ObjectId(USER_ID));
        when(mongoTemplate.findById(new ObjectId(USER_ID), UserDocument.class)).thenReturn(user);
        when(mongoTemplate.remove(user)).thenReturn(DeleteResult.acknowledged(1));

        assertThat(new UserDeleteService(mongoTemplate).delete(USER_ID)).isTrue();
    }

    @Test
    void returnsFalseForMissingAndMalformedUser() {
        var service = new UserDeleteService(mongoTemplate);

        assertThat(service.delete(USER_ID)).isFalse();
        assertThat(service.delete("not-an-object-id")).isFalse();
    }
}
