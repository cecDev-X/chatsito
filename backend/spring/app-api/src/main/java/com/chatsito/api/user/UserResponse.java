package com.chatsito.api.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponse(
        @JsonProperty("_id") String id,
        String name,
        String email,
        String password,
        String bio,
        String imageUrl,
        List<String> followers,
        List<String> following) {
    public static UserResponse from(UserDocument user) {
        return new UserResponse(
                user.getId().toHexString(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getBio(),
                user.getImageUrl(),
                user.getFollowers(),
                user.getFollowing());
    }
}
