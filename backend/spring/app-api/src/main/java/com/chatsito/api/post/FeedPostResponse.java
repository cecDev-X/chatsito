package com.chatsito.api.post;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record FeedPostResponse(
        @JsonProperty("_id") String id,
        String title,
        String message,
        String creator,
        String selectedFile,
        List<String> likes,
        Instant createdAt,
        @JsonInclude(JsonInclude.Include.NON_NULL) String name,
        @JsonProperty("CreatorImg")
        @JsonInclude(JsonInclude.Include.NON_NULL) String creatorImage,
        List<CommentResponse> comments) {
}
