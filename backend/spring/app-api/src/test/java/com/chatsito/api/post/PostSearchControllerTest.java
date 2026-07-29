package com.chatsito.api.post;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import com.chatsito.api.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PostSearchControllerTest {
    private PostSearchService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(PostSearchService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PostSearchController(service)).build();
    }

    @Test
    void returnsDoubleWrappedLegacySearchResponse() throws Exception {
        var user = new UserResponse(
                "user-id", "Visible User", "visible@example.com", "test-hash",
                "bio", null, List.of(), List.of());
        var post = new PostResponse(
                "post-id", "Visible Post", "message", "user-id", "",
                List.of(), Instant.parse("2026-07-28T12:00:00Z"));
        when(service.search("visible"))
                .thenReturn(new SearchResponse(new SearchDataResponse(List.of(user), List.of(post))));

        mockMvc.perform(get("/posts/search").queryParam("searchQuery", "visible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user[0]._id").value("user-id"))
                .andExpect(jsonPath("$.data.posts[0]._id").value("post-id"));
    }

    @Test
    void allowsMissingSearchQuery() throws Exception {
        when(service.search(null))
                .thenReturn(new SearchResponse(new SearchDataResponse(List.of(), List.of())));

        mockMvc.perform(get("/posts/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user").isEmpty())
                .andExpect(jsonPath("$.data.posts").isEmpty());
    }
}
