package com.chatsito.api.post;

import java.util.List;

import com.chatsito.api.user.UserResponse;

public record SearchDataResponse(List<UserResponse> user, List<PostResponse> posts) {
}
