package com.chatsito.api.post;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PostDetailsResponse(
        @JsonProperty("_id") String id,
        String title,
        String message,
        String creator,
        String selectedFile,
        List<String> likes,
        Instant createdAt,
        List<CommentResponse> comments) {
}
