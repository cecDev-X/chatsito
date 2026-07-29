package com.chatsito.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LegacyPasswordServiceTest {
    @Test
    void createsCompatibleBcryptHash() {
        var service = new LegacyPasswordService();

        String hash = service.hash("secret-password");

        assertThat(hash).startsWith("$2b$12$");
        assertThat(service.matches("secret-password", hash)).isTrue();
        assertThat(service.matches("wrong-password", hash)).isFalse();
    }
}
