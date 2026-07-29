package com.chatsito.api.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LegacyJwtService {
    private final String secret;
    private final String algorithm;
    private final ObjectMapper objectMapper;

    public LegacyJwtService(
            @Value("${legacy.jwt.secret:}") String secret,
            @Value("${legacy.jwt.algorithm:HS256}") String algorithm,
            ObjectMapper objectMapper) {
        this.secret = secret;
        this.algorithm = algorithm;
        this.objectMapper = objectMapper;
    }

    public String authenticate(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new LegacyAuthenticationException("Not authenticated");
        }
        var parts = authorization.split(" ", -1);
        if (parts.length != 2 || !"Bearer".equals(parts[0])) {
            throw new LegacyAuthenticationException("Invalid authentication schema");
        }

        try {
            var tokenParts = parts[1].split("\\.", -1);
            if (tokenParts.length != 3 || secret.isEmpty() || !"HS256".equals(algorithm)) {
                throw new IllegalArgumentException("Invalid token");
            }

            var header = objectMapper.readTree(decode(tokenParts[0]));
            if (!algorithm.equals(header.path("alg").asText())) {
                throw new IllegalArgumentException("Invalid algorithm");
            }

            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(
                    (tokenParts[0] + "." + tokenParts[1]).getBytes(StandardCharsets.US_ASCII));
            byte[] actual = Base64.getUrlDecoder().decode(tokenParts[2]);
            if (!MessageDigest.isEqual(expected, actual)) {
                throw new IllegalArgumentException("Invalid signature");
            }

            var payload = objectMapper.readTree(decode(tokenParts[1]));
            if (!payload.has("expires")
                    || payload.path("expires").asDouble() < Instant.now().getEpochSecond()
                    || !payload.path("user_id").isTextual()
                    || payload.path("user_id").asText().isEmpty()) {
                throw new IllegalArgumentException("Expired or invalid payload");
            }
            return payload.path("user_id").asText();
        } catch (LegacyAuthenticationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LegacyAuthenticationException("Invalid token or expired");
        }
    }

    public String sign(String userId) {
        try {
            if (secret.isEmpty() || !"HS256".equals(algorithm)) {
                throw new IllegalStateException("JWT signing is not configured");
            }
            var header = new LinkedHashMap<String, Object>();
            header.put("alg", algorithm);
            header.put("typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("user_id", userId);
            payload.put("expires", Instant.now().getEpochSecond() + 86400);

            var encoder = Base64.getUrlEncoder().withoutPadding();
            String headerPart = encoder.encodeToString(objectMapper.writeValueAsBytes(header));
            String payloadPart = encoder.encodeToString(objectMapper.writeValueAsBytes(payload));
            String content = headerPart + "." + payloadPart;
            String signature = encoder.encodeToString(signature(content));
            return content + "." + signature;
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign JWT", exception);
        }
    }

    private byte[] signature(String content) throws Exception {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(content.getBytes(StandardCharsets.US_ASCII));
    }

    private byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}
