package com.chatsito.api.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.chatsito.api.auth.LegacyJwtService;
import com.chatsito.api.config.LegacyValidationExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserFollowControllerTest {
    private static final String TARGET_ID = "000000000000000000000004";
    private static final String ACTOR_ID = "000000000000000000000001";

    private UserFollowService followService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        followService = Mockito.mock(UserFollowService.class);
        var jwtService = Mockito.mock(LegacyJwtService.class);
        when(jwtService.authenticate("Bearer valid-token")).thenReturn(ACTOR_ID);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserFollowController(followService, jwtService))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void returnsBothUpdatedUsers() throws Exception {
        var target = response(TARGET_ID, List.of(ACTOR_ID), List.of());
        var actor = response(ACTOR_ID, List.of(), List.of(TARGET_ID));
        when(followService.toggle(TARGET_ID, ACTOR_ID))
                .thenReturn(new UserFollowResponse(target, actor));

        performPatch(TARGET_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updateduser1.followers[0]").value(ACTOR_ID))
                .andExpect(jsonPath("$.updateduser2.following[0]").value(TARGET_ID));
    }

    @Test
    void returnsLegacyNullForFailedToggle() throws Exception {
        when(followService.toggle("not-an-object-id", ACTOR_ID)).thenReturn(null);

        performPatch("not-an-object-id")
                .andExpect(status().isOk())
                .andExpect(content().string("null"));
    }

    private UserResponse response(String id, List<String> followers, List<String> following) {
        return new UserResponse(id, "User", "user@example.com", "hash", "", null,
                followers, following);
    }

    private org.springframework.test.web.servlet.ResultActions performPatch(String id) throws Exception {
        return mockMvc.perform(patch("/user/{id}/following", id)
                .header("Authorization", "Bearer valid-token"));
    }
}
