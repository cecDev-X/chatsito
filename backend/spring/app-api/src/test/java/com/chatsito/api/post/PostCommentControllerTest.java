package com.chatsito.api.post;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

class PostCommentControllerTest {
    private static final String POST_ID = "300000000000000000000002";
    private static final String USER_ID = "000000000000000000000001";

    private PostCommentService commentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        commentService = Mockito.mock(PostCommentService.class);
        var jwtService = Mockito.mock(LegacyJwtService.class);
        when(jwtService.authenticate("Bearer valid-token")).thenReturn(USER_ID);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PostCommentController(commentService, jwtService))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void returnsCreatedPostEnvelope() throws Exception {
        var comment = new CommentResponse(
                "800000000000000000000001", POST_ID, USER_ID, "New comment",
                Instant.parse("2026-07-28T18:00:00Z"), null);
        var post = new PostDetailsResponse(
                POST_ID, "Post", "message", "creator", "", List.of(),
                Instant.parse("2026-07-28T11:00:00Z"), List.of(comment));
        when(commentService.create(POST_ID, USER_ID, "New comment"))
                .thenReturn(new SinglePostResponse(post));

        performPost(POST_ID, "{\"value\":\"New comment\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.post._id").value(POST_ID))
                .andExpect(jsonPath("$.post.comments[0].value").value("New comment"));
    }

    @Test
    void returnsLegacyNullBodyWhenServiceCannotBuildPost() throws Exception {
        when(commentService.create("not-an-object-id", USER_ID, "Orphan"))
                .thenReturn(null);

        performPost("not-an-object-id", "{\"value\":\"Orphan\"}")
                .andExpect(status().isCreated())
                .andExpect(content().string("null"));
    }

    private org.springframework.test.web.servlet.ResultActions performPost(String id, String body)
            throws Exception {
        return mockMvc.perform(post("/posts/{id}/commentPost", id)
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
