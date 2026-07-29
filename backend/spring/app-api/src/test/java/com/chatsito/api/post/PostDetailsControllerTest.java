package com.chatsito.api.post;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PostDetailsControllerTest {
    private static final String POST_ID = "300000000000000000000007";

    private PostDetailsService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(PostDetailsService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PostDetailsController(service)).build();
    }

    @Test
    void returnsLegacyPostEnvelope() throws Exception {
        var post = new PostDetailsResponse(
                POST_ID, "Details Post", "message", "creator", "", List.of(),
                Instant.parse("2026-07-28T16:00:00Z"), List.of());
        when(service.getPost(POST_ID)).thenReturn(new SinglePostResponse(post));

        mockMvc.perform(get("/posts/{id}", POST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post._id").value(POST_ID))
                .andExpect(jsonPath("$.post.comments").isArray());
    }

    @Test
    void returnsLegacyNotFoundResponse() throws Exception {
        when(service.getPost("not-an-object-id")).thenReturn(null);

        mockMvc.perform(get("/posts/{id}", "not-an-object-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("post not found."));
    }
}
