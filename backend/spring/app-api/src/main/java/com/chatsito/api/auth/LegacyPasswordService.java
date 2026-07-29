package com.chatsito.api.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LegacyPasswordService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(
            BCryptPasswordEncoder.BCryptVersion.$2B, 12);

    public String hash(String password) {
        return encoder.encode(password);
    }

    public boolean matches(String password, String hash) {
        return encoder.matches(password, hash);
    }
}
