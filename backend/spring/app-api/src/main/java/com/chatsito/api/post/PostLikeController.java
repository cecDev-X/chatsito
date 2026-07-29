package com.chatsito.api.post;

import java.util.Map;

import com.chatsito.api.auth.LegacyJwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostLikeController {
    private final PostLikeService postLikeService;
    private final LegacyJwtService legacyJwtService;

    public PostLikeController(PostLikeService postLikeService, LegacyJwtService legacyJwtService) {
        this.postLikeService = postLikeService;
        this.legacyJwtService = legacyJwtService;
    }

    @PatchMapping("/{id}/likePost")
    public ResponseEntity<?> likePost(
            @PathVariable String id,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        String userId = legacyJwtService.authenticate(authorization);
        var response = postLikeService.toggle(id, userId);
        if (response == null) {
            return ResponseEntity.badRequest().body(Map.of("detail", "can't Like the Post"));
        }
        return ResponseEntity.ok(response);
    }
}
