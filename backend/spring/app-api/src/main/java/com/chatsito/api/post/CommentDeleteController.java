package com.chatsito.api.post;

import java.util.Map;

import com.chatsito.api.auth.LegacyJwtService;
import com.fasterxml.jackson.databind.node.NullNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class CommentDeleteController {
    private final CommentDeleteService commentDeleteService;
    private final LegacyJwtService legacyJwtService;

    public CommentDeleteController(
            CommentDeleteService commentDeleteService,
            LegacyJwtService legacyJwtService) {
        this.commentDeleteService = commentDeleteService;
        this.legacyJwtService = legacyJwtService;
    }

    @DeleteMapping("/{id}/deleteComment")
    public ResponseEntity<?> deleteComment(
            @PathVariable String id,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        legacyJwtService.authenticate(authorization);
        Object response = commentDeleteService.delete(id)
                ? Map.of("message", "comment deleted successfully.")
                : NullNode.getInstance();
        return ResponseEntity.ok(response);
    }
}
