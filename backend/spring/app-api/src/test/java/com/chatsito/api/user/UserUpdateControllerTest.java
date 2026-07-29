package com.chatsito.api.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.chatsito.api.auth.LegacyJwtService;
import com.chatsito.api.config.LegacyValidationExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserUpdateControllerTest {
    private static final String USER_ID = "000000000000000000000001";
    private static final String BODY = "{\"name\":\"Updated Name\",\"bio\":\"Updated bio\","
            + "\"imageUrl\":\"updated.png\",\"email\":\"ignored@example.com\"}";

    private UserUpdateService updateService;
    private LegacyJwtService jwtService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        updateService = Mockito.mock(UserUpdateService.class);
        jwtService = Mockito.mock(LegacyJwtService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserUpdateController(updateService, jwtService))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void returnsUpdatedProfileEnvelope() throws Exception {
        var request = new UpdateUserRequest("Updated Name", "Updated bio", "updated.png");
        var user = new UserResponse(
                USER_ID, "Updated Name", "main@spring.test", "hash", "Updated bio",
                "updated.png", List.of(), List.of());
        when(jwtService.authenticate("Bearer valid-token")).thenReturn(USER_ID);
        when(updateService.update(USER_ID, request))
                .thenReturn(new UserProfileResponse(user, "posts"));

        performPatch(USER_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("Updated Name"))
                .andExpect(jsonPath("$.posts").value("posts"));
    }

    @Test
    void rejectsDifferentAuthenticatedUser() throws Exception {
        when(jwtService.authenticate("Bearer valid-token")).thenReturn("different-user");

        performPatch(USER_ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("you are not authorized to update this profile"));
    }

    @Test
    void returnsLegacyFailureForMissingUser() throws Exception {
        var request = new UpdateUserRequest("Updated Name", "Updated bio", "updated.png");
        when(jwtService.authenticate("Bearer valid-token")).thenReturn(USER_ID);
        when(updateService.update(USER_ID, request)).thenReturn(null);

        performPatch(USER_ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("can't update user data"));
    }

    private org.springframework.test.web.servlet.ResultActions performPatch(String id) throws Exception {
        return mockMvc.perform(patch("/user/Update/{id}", id)
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY));
    }
}
