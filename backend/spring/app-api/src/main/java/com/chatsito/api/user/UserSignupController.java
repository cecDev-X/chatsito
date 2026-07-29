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
public class UserSignupController {
    private final UserSignupService userSignupService;

    public UserSignupController(UserSignupService userSignupService) {
        this.userSignupService = userSignupService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            return ResponseEntity.status(201).body(userSignupService.signup(request));
        } catch (RuntimeException exception) {
            String message = exception.getMessage() == null ? "" : exception.getMessage();
            return ResponseEntity.badRequest().body(Map.of("message", message));
        }
    }
}
