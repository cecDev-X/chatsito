package com.chatsito.api.post;

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

class PostFeedControllerTest {
    private PostFeedService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(PostFeedService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PostFeedController(service)).build();
    }

    @Test
    void returnsLegacyFeedEnvelope() throws Exception {
        when(service.getFeed("1", "main-user", null))
                .thenReturn(new PostFeedResponse(List.of(), 1, 3));

        mockMvc.perform(get("/posts")
                        .queryParam("page", "1")
                        .queryParam("id", "main-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.numberOfPages").value(3));
    }

    @Test
    void usesLegacyFallbackWhenServiceFails() throws Exception {
        when(service.getFeed(null, null, null)).thenReturn(null);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.numberOfPages").value(0));
    }
}
