package com.chatsito.api.user;

import java.util.List;

import com.chatsito.api.auth.LegacyJwtService;
import com.chatsito.api.auth.LegacyPasswordService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserSignupService {
    private final MongoTemplate mongoTemplate;
    private final LegacyPasswordService passwordService;
    private final LegacyJwtService jwtService;

    public UserSignupService(
            MongoTemplate mongoTemplate,
            LegacyPasswordService passwordService,
            LegacyJwtService jwtService) {
        this.mongoTemplate = mongoTemplate;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public AuthResponse signup(SignupRequest request) {
        var user = new UserDocument();
        user.setName(request.firstName() + " " + request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordService.hash(request.password()));
        user.setBio("");
        user.setImageUrl(null);
        user.setFollowers(List.of());
        user.setFollowing(List.of());

        var saved = mongoTemplate.save(user);
        String id = saved.getId().toHexString();
        var result = new AuthUserResponse(id, saved.getName(), saved.getEmail());
        return new AuthResponse(result, jwtService.sign(id));
    }
}
