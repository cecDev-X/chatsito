package com.chatsito.api.post;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import com.chatsito.api.auth.LegacyJwtService;
import com.chatsito.api.config.LegacyValidationExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PostUpdateControllerTest {
    private static final String POST_ID = "300000000000000000000001";
    private static final String OWNER_ID = "000000000000000000000001";
    private static final String BODY = "{\"message\":\"Updated message\","
            + "\"selectedFile\":\"\",\"title\":\"Updated title\",\"_id\":\"ignored\"}";

    private PostUpdateService updateService;
    private LegacyJwtService jwtService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        updateService = Mockito.mock(PostUpdateService.class);
        jwtService = Mockito.mock(LegacyJwtService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PostUpdateController(updateService, jwtService))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
        when(jwtService.authenticate("Bearer valid-token")).thenReturn(OWNER_ID);
    }

    @Test
    void returnsLegacyUpdatedPostEnvelope() throws Exception {
        var request = new CreatePostRequest("Updated message", "", "Updated title");
        var post = new PostResponse(
                POST_ID, "Updated title", "Updated message", OWNER_ID, "", List.of(),
                Instant.parse("2026-07-28T10:00:00Z"));
        when(updateService.update(POST_ID, OWNER_ID, request))
                .thenReturn(PostUpdateResult.success(post));

        performPatch(POST_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data._id").value(POST_ID))
                .andExpect(jsonPath("$.data.title").value("Updated title"));
    }

    @Test
    void returnsLegacyMissingAndUnauthorizedResponses() throws Exception {
        var request = new CreatePostRequest("Updated message", "", "Updated title");
        when(updateService.update("300000000000000000000099", OWNER_ID, request))
                .thenReturn(PostUpdateResult.failure(PostUpdateResult.Status.NOT_FOUND));
        when(updateService.update(POST_ID, OWNER_ID, request))
                .thenReturn(PostUpdateResult.failure(PostUpdateResult.Status.NOT_AUTHORIZED));

        performPatch("300000000000000000000099")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("post not found."));
        performPatch(POST_ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("you are not authoriezd to upate this post."));
    }

    private org.springframework.test.web.servlet.ResultActions performPatch(String id) throws Exception {
        return mockMvc.perform(patch("/posts/{id}", id)
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY));
    }
}
