package com.chatsito.api.post;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostFeedController {
    private final PostFeedService postFeedService;

    public PostFeedController(PostFeedService postFeedService) {
        this.postFeedService = postFeedService;
    }

    @GetMapping
    public PostFeedResponse getPosts(
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String profileId) {
        var response = postFeedService.getFeed(page, id, profileId);
        return response == null ? new PostFeedResponse(List.of(), 1, 0) : response;
    }
}
