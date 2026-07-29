package com.chatsito.api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.chatsito.api.auth.LegacyJwtService;
import com.chatsito.api.auth.LegacyPasswordService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class UserSignupServiceTest {
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private LegacyPasswordService passwordService;
    @Mock
    private LegacyJwtService jwtService;

    @Test
    void createsLegacyUserAndReturnsAuthenticationResponse() {
        when(passwordService.hash("password")).thenReturn("bcrypt-hash");
        when(mongoTemplate.save(any(UserDocument.class))).thenAnswer(invocation -> {
            UserDocument user = invocation.getArgument(0);
            user.setId(new ObjectId("a00000000000000000000001"));
            return user;
        });
        when(jwtService.sign("a00000000000000000000001")).thenReturn("signed-token");

        var response = new UserSignupService(mongoTemplate, passwordService, jwtService)
                .signup(new SignupRequest("Spring", "User", "spring@example.com", "password"));

        assertThat(response.result().name()).isEqualTo("Spring User");
        assertThat(response.result().email()).isEqualTo("spring@example.com");
        assertThat(response.token()).isEqualTo("signed-token");
    }
}
