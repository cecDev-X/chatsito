package com.chatsito.api.post;

import java.util.Map;

import com.chatsito.api.auth.LegacyJwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostCreationController {
    private final PostCreationService postCreationService;
    private final LegacyJwtService legacyJwtService;

    public PostCreationController(
            PostCreationService postCreationService,
            LegacyJwtService legacyJwtService) {
        this.postCreationService = postCreationService;
        this.legacyJwtService = legacyJwtService;
    }

    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreatePostRequest request) {
        String userId = legacyJwtService.authenticate(authorization);
        var response = postCreationService.create(request, userId);
        if (response == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "can't create post"));
        }
        return ResponseEntity.status(201).body(response);
    }
}
