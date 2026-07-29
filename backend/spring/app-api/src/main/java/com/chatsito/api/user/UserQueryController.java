package com.chatsito.api.user;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserQueryController {
    private final UserQueryService userQueryService;

    public UserQueryController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping("/getUser/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        var profile = userQueryService.getProfile(id);
        if (profile == null) {
            return ResponseEntity.status(404).body(Map.of("message", "user not found."));
        }
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/getSug")
    public UserSuggestionsResponse getSuggestions(@RequestParam(required = false) String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return userQueryService.getSuggestions(id);
    }
}
