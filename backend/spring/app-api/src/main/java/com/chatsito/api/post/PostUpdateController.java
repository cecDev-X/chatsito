package com.chatsito.api.post;

import java.util.Map;

import com.chatsito.api.auth.LegacyJwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostUpdateController {
    private final PostUpdateService postUpdateService;
    private final LegacyJwtService legacyJwtService;

    public PostUpdateController(PostUpdateService postUpdateService, LegacyJwtService legacyJwtService) {
        this.postUpdateService = postUpdateService;
        this.legacyJwtService = legacyJwtService;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable String id,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreatePostRequest request) {
        String userId = legacyJwtService.authenticate(authorization);
        var result = postUpdateService.update(id, userId, request);
        return switch (result.status()) {
            case SUCCESS -> ResponseEntity.ok(new UpdatedPostResponse(result.post()));
            case NOT_FOUND -> ResponseEntity.status(404).body(Map.of("message", "post not found."));
            case NOT_AUTHORIZED -> ResponseEntity.badRequest()
                    .body(Map.of("error", "you are not authoriezd to upate this post."));
            case FAILED -> ResponseEntity.badRequest().body(Map.of("error", "can't update post."));
        };
    }
}
