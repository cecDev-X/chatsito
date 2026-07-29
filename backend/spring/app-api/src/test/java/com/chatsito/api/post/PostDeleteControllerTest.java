package com.chatsito.api.post;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chatsito.api.auth.LegacyJwtService;
import com.chatsito.api.config.LegacyValidationExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PostDeleteControllerTest {
    private static final String POST_ID = "300000000000000000000007";
    private static final String OWNER_ID = "000000000000000000000004";

    private PostDeleteService deleteService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        deleteService = Mockito.mock(PostDeleteService.class);
        var jwtService = Mockito.mock(LegacyJwtService.class);
        when(jwtService.authenticate("Bearer valid-token")).thenReturn(OWNER_ID);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PostDeleteController(deleteService, jwtService))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void returnsLegacySuccessMessage() throws Exception {
        when(deleteService.delete(POST_ID, OWNER_ID)).thenReturn(PostDeleteResult.SUCCESS);

        performDelete(POST_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("post deleted successfully."));
    }

    @Test
    void returnsLegacyMissingUnauthorizedAndFailureResponses() throws Exception {
        when(deleteService.delete("missing", OWNER_ID)).thenReturn(PostDeleteResult.NOT_FOUND);
        when(deleteService.delete("unauthorized", OWNER_ID))
                .thenReturn(PostDeleteResult.NOT_AUTHORIZED);
        when(deleteService.delete("malformed", OWNER_ID)).thenReturn(PostDeleteResult.FAILED);

        performDelete("missing")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("post not found."));
        performDelete("unauthorized")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("you are not authoriezd to delete this post."));
        performDelete("malformed")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("can't delete post."));
    }

    private org.springframework.test.web.servlet.ResultActions performDelete(String id) throws Exception {
        return mockMvc.perform(delete("/posts/{id}", id)
                .header("Authorization", "Bearer valid-token"));
    }
}
