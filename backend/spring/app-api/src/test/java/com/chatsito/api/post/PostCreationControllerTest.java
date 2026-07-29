package com.chatsito.api.post;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import com.chatsito.api.auth.LegacyAuthenticationException;
import com.chatsito.api.auth.LegacyJwtService;
import com.chatsito.api.config.LegacyValidationExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PostCreationControllerTest {
    private PostCreationService creationService;
    private LegacyJwtService jwtService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        creationService = Mockito.mock(PostCreationService.class);
        jwtService = Mockito.mock(LegacyJwtService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PostCreationController(creationService, jwtService))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void returnsCreatedPost() throws Exception {
        var request = new CreatePostRequest("message", "", "Created Post");
        var response = new PostResponse(
                "600000000000000000000001", "Created Post", "message", "user-id", "",
                List.of(), Instant.parse("2026-07-28T18:00:00Z"));
        when(jwtService.authenticate("Bearer valid-token")).thenReturn("user-id");
        when(creationService.create(request, "user-id")).thenReturn(response);

        mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"message\",\"selectedFile\":\"\",\"title\":\"Created Post\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._id").value("600000000000000000000001"))
                .andExpect(jsonPath("$.creator").value("user-id"))
                .andExpect(jsonPath("$.likes").isEmpty());
    }

    @Test
    void returnsUnprocessableEntityForMissingBodyField() throws Exception {
        mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"message\",\"title\":\"Created Post\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail[0].loc[0]").value("body"))
                .andExpect(jsonPath("$.detail[0].loc[1]").value("selectedFile"));
    }

    @Test
    void returnsLegacyForbiddenResponseForInvalidToken() throws Exception {
        when(jwtService.authenticate("Bearer invalid"))
                .thenThrow(new LegacyAuthenticationException("Invalid token or expired"));

        mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"message\",\"selectedFile\":\"\",\"title\":\"Created Post\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Invalid token or expired"));
    }

    @Test
    void returnsLegacyBadRequestWhenWriteFails() throws Exception {
        var request = new CreatePostRequest("message", "", "Created Post");
        when(jwtService.authenticate("Bearer valid-token")).thenReturn("user-id");
        when(creationService.create(request, "user-id")).thenReturn(null);

        mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"message\",\"selectedFile\":\"\",\"title\":\"Created Post\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("can't create post"));
    }
}
