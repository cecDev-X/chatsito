package com.chatsito.api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.chatsito.api.auth.LegacyJwtService;
import com.chatsito.api.auth.LegacyPasswordService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class UserSigninServiceTest {
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private LegacyPasswordService passwordService;
    @Mock
    private LegacyJwtService jwtService;

    @Test
    void authenticatesAndReturnsLegacyEnvelope() {
        var user = user();
        when(mongoTemplate.findOne(any(Query.class), eq(UserDocument.class))).thenReturn(user);
        when(passwordService.matches("spring-password", "stored-bcrypt")).thenReturn(true);
        when(jwtService.sign("000000000000000000000001")).thenReturn("signed-token");

        var response = new UserSigninService(mongoTemplate, passwordService, jwtService)
                .signin(new SigninRequest("main@spring.test", "spring-password"));

        assertThat(response.result().name()).isEqualTo("Spring Main");
        assertThat(response.token()).isEqualTo("signed-token");
    }

    @Test
    void returnsNullForMissingUserOrWrongPassword() {
        var service = new UserSigninService(mongoTemplate, passwordService, jwtService);
        assertThat(service.signin(new SigninRequest("missing@example.com", "password"))).isNull();

        when(mongoTemplate.findOne(any(Query.class), eq(UserDocument.class))).thenReturn(user());
        when(passwordService.matches("wrong", "stored-bcrypt")).thenReturn(false);
        assertThat(service.signin(new SigninRequest("main@spring.test", "wrong"))).isNull();
    }

    private UserDocument user() {
        var user = new UserDocument();
        user.setId(new ObjectId("000000000000000000000001"));
        user.setName("Spring Main");
        user.setEmail("main@spring.test");
        user.setPassword("stored-bcrypt");
        return user;
    }
}
