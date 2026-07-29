package com.chatsito.api.post;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CommentResponse(
        @JsonProperty("_id") String id,
        String postId,
        String userId,
        String value,
        Instant createdAt,
        @JsonInclude(JsonInclude.Include.NON_NULL) CommentUserResponse user) {
}
