package com.chatsito.api.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chatsito.api.config.LegacyValidationExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserSigninControllerTest {
    private UserSigninService signinService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        signinService = Mockito.mock(UserSigninService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserSigninController(signinService))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void returnsAuthenticationResponse() throws Exception {
        var request = new SigninRequest("main@spring.test", "spring-password");
        when(signinService.signin(request)).thenReturn(new AuthResponse(
                new AuthUserResponse("user-id", "Spring Main", "main@spring.test"),
                "signed-token"));

        performSignin("{\"email\":\"main@spring.test\",\"password\":\"spring-password\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value("user-id"))
                .andExpect(jsonPath("$.token").value("signed-token"));
    }

    @Test
    void returnsLegacyCredentialsError() throws Exception {
        var request = new SigninRequest("main@spring.test", "wrong");
        when(signinService.signin(request)).thenReturn(null);

        performSignin("{\"email\":\"main@spring.test\",\"password\":\"wrong\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("user with provided credentials is not found."));
    }

    private org.springframework.test.web.servlet.ResultActions performSignin(String body) throws Exception {
        return mockMvc.perform(post("/user/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
