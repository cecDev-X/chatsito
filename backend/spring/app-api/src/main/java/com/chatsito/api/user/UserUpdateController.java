package com.chatsito.api.user;

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
@RequestMapping("/user")
public class UserUpdateController {
    private final UserUpdateService userUpdateService;
    private final LegacyJwtService legacyJwtService;

    public UserUpdateController(UserUpdateService userUpdateService, LegacyJwtService legacyJwtService) {
        this.userUpdateService = userUpdateService;
        this.legacyJwtService = legacyJwtService;
    }

    @PatchMapping("/Update/{id}")
    public ResponseEntity<?> update(
            @PathVariable String id,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpdateUserRequest request) {
        String authenticatedId = legacyJwtService.authenticate(authorization);
        if (!authenticatedId.equals(id)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "you are not authorized to update this profile"));
        }

        var response = userUpdateService.update(id, request);
        if (response == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "can't update user data"));
        }
        return ResponseEntity.ok(response);
    }
}
