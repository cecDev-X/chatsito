package com.chatsito.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class LegacyJwtServiceTest {
    private static final String SECRET = "test-secret";

    @Test
    void acceptsLegacyHs256Token() throws Exception {
        var service = new LegacyJwtService(SECRET, "HS256", new ObjectMapper());
        var token = token(Instant.now().plusSeconds(60).getEpochSecond());

        assertThat(service.authenticate("Bearer " + token)).isEqualTo("legacy-user");
    }

    @Test
    void signsTokenAcceptedByLegacyVerifier() {
        var service = new LegacyJwtService(SECRET, "HS256", new ObjectMapper());

        String token = service.sign("spring-user");

        assertThat(service.authenticate("Bearer " + token)).isEqualTo("spring-user");
    }

    @Test
    void rejectsExpiredTamperedAndMissingTokens() throws Exception {
        var service = new LegacyJwtService(SECRET, "HS256", new ObjectMapper());

        assertThatThrownBy(() -> service.authenticate(null))
                .isInstanceOf(LegacyAuthenticationException.class)
                .hasMessage("Not authenticated");
        assertThatThrownBy(() -> service.authenticate("Bearer " + token(0)))
                .isInstanceOf(LegacyAuthenticationException.class)
                .hasMessage("Invalid token or expired");
        assertThatThrownBy(() -> service.authenticate("Bearer invalid.token.value"))
                .isInstanceOf(LegacyAuthenticationException.class)
                .hasMessage("Invalid token or expired");
    }

    private String token(long expires) throws Exception {
        var encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString(
                "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payload = encoder.encodeToString(
                ("{\"user_id\":\"legacy-user\",\"expires\":" + expires + "}")
                        .getBytes(StandardCharsets.UTF_8));
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = encoder.encodeToString(
                mac.doFinal((header + "." + payload).getBytes(StandardCharsets.US_ASCII)));
        return header + "." + payload + "." + signature;
    }
}
