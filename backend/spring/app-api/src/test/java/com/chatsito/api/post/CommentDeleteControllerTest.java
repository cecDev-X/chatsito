package com.chatsito.api.post;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chatsito.api.auth.LegacyJwtService;
import com.chatsito.api.config.LegacyValidationExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CommentDeleteControllerTest {
    private static final String COMMENT_ID = "500000000000000000000001";

    private CommentDeleteService deleteService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        deleteService = Mockito.mock(CommentDeleteService.class);
        var jwtService = Mockito.mock(LegacyJwtService.class);
        when(jwtService.authenticate("Bearer valid-token")).thenReturn("authenticated-user");
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CommentDeleteController(deleteService, jwtService))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void returnsLegacySuccessMessage() throws Exception {
        when(deleteService.delete(COMMENT_ID)).thenReturn(true);

        performDelete(COMMENT_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("comment deleted successfully."));
    }

    @Test
    void returnsLegacyNullForMissingOrMalformedComment() throws Exception {
        performDelete("not-an-object-id")
                .andExpect(status().isOk())
                .andExpect(content().string("null"));
    }

    private org.springframework.test.web.servlet.ResultActions performDelete(String id) throws Exception {
        return mockMvc.perform(delete("/posts/{id}/deleteComment", id)
                .header("Authorization", "Bearer valid-token"));
    }
}
