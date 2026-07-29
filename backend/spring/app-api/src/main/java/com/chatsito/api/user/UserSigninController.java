package com.chatsito.api.user;

import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserSigninController {
    private final UserSigninService userSigninService;

    public UserSigninController(UserSigninService userSigninService) {
        this.userSigninService = userSigninService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody SigninRequest request) {
        var response = userSigninService.signin(request);
        if (response == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "user with provided credentials is not found."));
        }
        return ResponseEntity.ok(response);
    }
}
