package com.chatsito.api.post;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostDetailsController {
    private final PostDetailsService postDetailsService;

    public PostDetailsController(PostDetailsService postDetailsService) {
        this.postDetailsService = postDetailsService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable String id) {
        var response = postDetailsService.getPost(id);
        if (response == null) {
            return ResponseEntity.status(404).body(Map.of("message", "post not found."));
        }
        return ResponseEntity.ok(response);
    }
}
