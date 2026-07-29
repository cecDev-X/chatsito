package com.chatsito.api.user;

import java.util.Map;

import com.chatsito.api.auth.LegacyJwtService;
import com.fasterxml.jackson.databind.node.NullNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserFollowController {
    private final UserFollowService userFollowService;
    private final LegacyJwtService legacyJwtService;

    public UserFollowController(UserFollowService userFollowService, LegacyJwtService legacyJwtService) {
        this.userFollowService = userFollowService;
        this.legacyJwtService = legacyJwtService;
    }

    @PatchMapping("/{id}/following")
    public ResponseEntity<?> toggle(
            @PathVariable String id,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        String actorId = legacyJwtService.authenticate(authorization);
        if (actorId.equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "cannot follow yourself"));
        }
        var response = userFollowService.toggle(id, actorId);
        return ResponseEntity.ok(response == null ? NullNode.getInstance() : response);
    }
}
