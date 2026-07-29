package com.chatsito.api.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserQueryControllerTest {
    private static final String USER_ID = "000000000000000000000001";

    private UserQueryService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(UserQueryService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserQueryController(service)).build();
    }

    @Test
    void returnsProfileWithLegacyFields() throws Exception {
        var user = new UserResponse(
                USER_ID,
                "Main User",
                "main@example.com",
                "hashed-password",
                "bio",
                null,
                List.of(),
                List.of());
        when(service.getProfile(USER_ID)).thenReturn(new UserProfileResponse(user, "posts"));

        mockMvc.perform(get("/user/getUser/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user._id").value(USER_ID))
                .andExpect(jsonPath("$.user.password").value("hashed-password"))
                .andExpect(jsonPath("$.posts").value("posts"));
    }

    @Test
    void returnsLegacyNotFoundResponse() throws Exception {
        when(service.getProfile("bad-id")).thenReturn(null);

        mockMvc.perform(get("/user/getUser/bad-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("user not found."));
    }

    @Test
    void returnsSuggestedUsersEnvelope() throws Exception {
        var suggested = new UserResponse(
                USER_ID,
                "Suggested User",
                "suggested@example.com",
                "hashed-password",
                "bio",
                null,
                List.of(),
                List.of());
        when(service.getSuggestions(USER_ID))
                .thenReturn(new UserSuggestionsResponse(List.of(suggested)));

        mockMvc.perform(get("/user/getSug").queryParam("id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0]._id").value(USER_ID))
                .andExpect(jsonPath("$.users[0].name").value("Suggested User"));
    }
}
