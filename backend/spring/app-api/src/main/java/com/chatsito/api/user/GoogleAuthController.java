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
public class GoogleAuthController {
    private final GoogleAuthService googleAuthService;

    public GoogleAuthController(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticate(@Valid @RequestBody GoogleAuthRequest request) {
        try {
            return ResponseEntity.ok(googleAuthService.authenticate(request.credential()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.internalServerError().body(Map.of("message", exception.getMessage()));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", "No se pudo iniciar sesión con Google."));
        }
    }
}
