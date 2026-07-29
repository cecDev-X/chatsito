package com.chatsito.api.post;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PostResponse(
        @JsonProperty("_id") String id,
        String title,
        String message,
        String creator,
        String selectedFile,
        List<String> likes,
        Instant createdAt) {
    static PostResponse from(PostDocument post) {
        return new PostResponse(
                post.getId().toHexString(),
                post.getTitle(),
                post.getMessage(),
                post.getCreator(),
                post.getSelectedFile(),
                post.getLikes(),
                post.getCreatedAt());
    }
}
