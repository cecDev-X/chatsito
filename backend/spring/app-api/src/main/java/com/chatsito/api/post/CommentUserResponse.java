package com.chatsito.api.post;

import com.chatsito.api.user.UserDocument;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CommentUserResponse(
        @JsonProperty("_id") String id,
        String name,
        String imageUrl) {
    static CommentUserResponse from(UserDocument user) {
        return new CommentUserResponse(
                user.getId().toHexString(), user.getName(), user.getImageUrl());
    }
}
