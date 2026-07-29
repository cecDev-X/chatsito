package com.chatsito.api.post;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostSearchController {
    private final PostSearchService postSearchService;

    public PostSearchController(PostSearchService postSearchService) {
        this.postSearchService = postSearchService;
    }

    @GetMapping("/search")
    public SearchResponse search(@RequestParam(required = false) String searchQuery) {
        return postSearchService.search(searchQuery);
    }
}
