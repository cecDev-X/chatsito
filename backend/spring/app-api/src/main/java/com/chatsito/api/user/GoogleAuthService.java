package com.chatsito.api.user;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.chatsito.api.auth.LegacyJwtService;
import com.chatsito.api.auth.LegacyPasswordService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GoogleAuthService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);

    private final MongoTemplate mongoTemplate;
    private final LegacyPasswordService passwordService;
    private final LegacyJwtService jwtService;
    private final RestClient restClient;
    private final String clientId;

    public GoogleAuthService(
            MongoTemplate mongoTemplate,
            LegacyPasswordService passwordService,
            LegacyJwtService jwtService,
            @Value("${google.client-id:}") String clientId) {
        this.mongoTemplate = mongoTemplate;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.restClient = RestClient.create();
        this.clientId = clientId.trim();
    }

    public AuthResponse authenticate(String credential) {
        if (clientId.isBlank()) {
            throw new IllegalStateException("Google authentication is not configured");
        }

        GoogleTokenInfo token = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("oauth2.googleapis.com")
                        .path("/tokeninfo")
                        .queryParam("id_token", credential)
                        .build())
                .retrieve()
                .body(GoogleTokenInfo.class);

        if (token == null) {
            throw new IllegalArgumentException("Invalid Google credential");
        }
        if (token.audience() == null || !clientId.equals(token.audience().trim())) {
            logger.warn("Google token audience does not match the configured client ID");
            throw new IllegalArgumentException("El Client ID de Google no coincide con el configurado.");
        }
        if (!isVerified(token.emailVerified())) {
            throw new IllegalArgumentException("El correo de Google no está verificado.");
        }
        if (token.email() == null || token.email().isBlank()) {
            throw new IllegalArgumentException("Google no devolvió un correo válido.");
        }

        String email = token.email().trim().toLowerCase(Locale.ROOT);
        UserDocument user = mongoTemplate.findOne(
                Query.query(Criteria.where("email").is(email)), UserDocument.class);
        if (user == null) {
            user = new UserDocument();
            user.setName(validName(token.name(), email));
            user.setEmail(email);
            user.setPassword(passwordService.hash(UUID.randomUUID().toString()));
            user.setBio("");
            user.setImageUrl(token.picture());
            user.setFollowers(List.of());
            user.setFollowing(List.of());
            user = mongoTemplate.save(user);
        }

        return new AuthResponse(
                new AuthUserResponse(user.getId().toHexString(), user.getName(), user.getEmail()),
                jwtService.sign(user.getId().toHexString()));
    }

    private String validName(String name, String email) {
        return name == null || name.isBlank() ? email.substring(0, email.indexOf('@')) : name;
    }

    private boolean isVerified(JsonNode emailVerified) {
        return emailVerified != null
                && ((emailVerified.isBoolean() && emailVerified.booleanValue())
                || (emailVerified.isTextual() && "true".equalsIgnoreCase(emailVerified.textValue())));
    }

    private record GoogleTokenInfo(
            String email,
            @JsonProperty("email_verified") JsonNode emailVerified,
            @JsonProperty("aud") String audience,
            String name,
            String picture) {
    }
}
