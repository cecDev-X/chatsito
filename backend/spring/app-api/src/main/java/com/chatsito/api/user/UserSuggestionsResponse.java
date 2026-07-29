package com.chatsito.api.user;

import java.util.List;

public record UserSuggestionsResponse(List<UserResponse> users) {
}
