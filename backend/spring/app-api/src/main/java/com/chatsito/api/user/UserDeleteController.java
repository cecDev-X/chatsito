package com.chatsito.api.user;

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
@RequestMapping("/user")
public class UserDeleteController {
    private final UserDeleteService userDeleteService;
    private final LegacyJwtService legacyJwtService;

    public UserDeleteController(UserDeleteService userDeleteService, LegacyJwtService legacyJwtService) {
        this.userDeleteService = userDeleteService;
        this.legacyJwtService = legacyJwtService;
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(
            @PathVariable String id,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        String authenticatedId = legacyJwtService.authenticate(authorization);
        if (!authenticatedId.equals(id)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "you are not authorized to delete this profile"));
        }

        Object response = userDeleteService.delete(id)
                ? Map.of("message", "user Delted Successfully.")
                : NullNode.getInstance();
        return ResponseEntity.ok(response);
    }
}
