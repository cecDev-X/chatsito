package com.chatsito.api.post;

import java.util.Map;

import com.chatsito.api.auth.LegacyJwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostDeleteController {
    private final PostDeleteService postDeleteService;
    private final LegacyJwtService legacyJwtService;

    public PostDeleteController(PostDeleteService postDeleteService, LegacyJwtService legacyJwtService) {
        this.postDeleteService = postDeleteService;
        this.legacyJwtService = legacyJwtService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(
            @PathVariable String id,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        String userId = legacyJwtService.authenticate(authorization);
        return switch (postDeleteService.delete(id, userId)) {
            case SUCCESS -> ResponseEntity.ok(Map.of("message", "post deleted successfully."));
            case NOT_FOUND -> ResponseEntity.status(404).body(Map.of("message", "post not found."));
            case NOT_AUTHORIZED -> ResponseEntity.badRequest()
                    .body(Map.of("error", "you are not authoriezd to delete this post."));
            case FAILED -> ResponseEntity.badRequest().body(Map.of("error", "can't delete post."));
        };
    }
}
