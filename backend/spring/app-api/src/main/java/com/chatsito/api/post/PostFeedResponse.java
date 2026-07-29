package com.chatsito.api.post;

import java.util.List;

public record PostFeedResponse(
        List<FeedPostResponse> data,
        int currentPage,
        int numberOfPages) {
}
