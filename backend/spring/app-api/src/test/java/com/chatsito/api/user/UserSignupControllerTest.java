package com.chatsito.api.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chatsito.api.config.LegacyValidationExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserSignupControllerTest {
    private UserSignupService signupService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        signupService = Mockito.mock(UserSignupService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserSignupController(signupService))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void returnsLegacyAuthenticationEnvelope() throws Exception {
        var request = new SignupRequest("Spring", "User", "spring@example.com", "password");
        var response = new AuthResponse(
                new AuthUserResponse("user-id", "Spring User", "spring@example.com"),
                "signed-token");
        when(signupService.signup(request)).thenReturn(response);

        performSignup("{\"firstName\":\"Spring\",\"lastName\":\"User\","
                        + "\"email\":\"spring@example.com\",\"password\":\"password\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.id").value("user-id"))
                .andExpect(jsonPath("$.result.name").value("Spring User"))
                .andExpect(jsonPath("$.token").value("signed-token"));
    }

    @Test
    void returnsBadRequestForDuplicateEmail() throws Exception {
        var request = new SignupRequest("Spring", "User", "spring@example.com", "password");
        when(signupService.signup(request)).thenThrow(new DuplicateKeyException("duplicate email"));

        performSignup("{\"firstName\":\"Spring\",\"lastName\":\"User\","
                        + "\"email\":\"spring@example.com\",\"password\":\"password\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("duplicate email"));
    }

    @Test
    void returnsUnprocessableEntityForInvalidEmail() throws Exception {
        performSignup("{\"firstName\":\"Spring\",\"lastName\":\"User\","
                        + "\"email\":\"not-an-email\",\"password\":\"password\"}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail[0].type").value("value_error"))
                .andExpect(jsonPath("$.detail[0].loc[1]").value("email"));
    }

    private org.springframework.test.web.servlet.ResultActions performSignup(String body) throws Exception {
        return mockMvc.perform(post("/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
