package com.chatsito.api.post;

import com.chatsito.api.auth.LegacyJwtService;
import com.fasterxml.jackson.databind.node.NullNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostCommentController {
    private final PostCommentService postCommentService;
    private final LegacyJwtService legacyJwtService;

    public PostCommentController(
            PostCommentService postCommentService,
            LegacyJwtService legacyJwtService) {
        this.postCommentService = postCommentService;
        this.legacyJwtService = legacyJwtService;
    }

    @PostMapping("/{id}/commentPost")
    public ResponseEntity<?> createComment(
            @PathVariable String id,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody CommentCreateRequest request) {
        String userId = legacyJwtService.authenticate(authorization);
        var response = postCommentService.create(id, userId, request.value());
        return ResponseEntity.status(201).body(
                response == null ? NullNode.getInstance() : response);
    }
}
