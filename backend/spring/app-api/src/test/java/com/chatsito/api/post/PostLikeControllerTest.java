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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PostLikeControllerTest {
    private static final String POST_ID = "300000000000000000000002";
    private static final String USER_ID = "000000000000000000000001";

    private PostLikeService likeService;
    private LegacyJwtService jwtService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        likeService = Mockito.mock(PostLikeService.class);
        jwtService = Mockito.mock(LegacyJwtService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PostLikeController(likeService, jwtService))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
        when(jwtService.authenticate("Bearer valid-token")).thenReturn(USER_ID);
    }

    @Test
    void returnsUpdatedPostDirectly() throws Exception {
        var post = new PostResponse(
                POST_ID, "Post", "message", "creator", "", List.of(USER_ID),
                Instant.parse("2026-07-28T11:00:00Z"));
        when(likeService.toggle(POST_ID, USER_ID)).thenReturn(post);

        performPatch(POST_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value(POST_ID))
                .andExpect(jsonPath("$.likes[0]").value(USER_ID));
    }

    @Test
    void returnsLegacyBadRequestForAnyPostFailure() throws Exception {
        when(likeService.toggle("not-an-object-id", USER_ID)).thenReturn(null);

        performPatch("not-an-object-id")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("can't Like the Post"));
    }

    private org.springframework.test.web.servlet.ResultActions performPatch(String id) throws Exception {
        return mockMvc.perform(patch("/posts/{id}/likePost", id)
                .header("Authorization", "Bearer valid-token"));
    }
}
