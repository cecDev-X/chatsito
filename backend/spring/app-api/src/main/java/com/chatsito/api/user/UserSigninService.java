package com.chatsito.api.user;

import com.chatsito.api.auth.LegacyJwtService;
import com.chatsito.api.auth.LegacyPasswordService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class UserSigninService {
    private final MongoTemplate mongoTemplate;
    private final LegacyPasswordService passwordService;
    private final LegacyJwtService jwtService;

    public UserSigninService(
            MongoTemplate mongoTemplate,
            LegacyPasswordService passwordService,
            LegacyJwtService jwtService) {
        this.mongoTemplate = mongoTemplate;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public AuthResponse signin(SigninRequest request) {
        var user = mongoTemplate.findOne(
                Query.query(Criteria.where("email").is(request.email())),
                UserDocument.class);
        if (user == null || !passwordService.matches(request.password(), user.getPassword())) {
            return null;
        }

        String id = user.getId().toHexString();
        return new AuthResponse(
                new AuthUserResponse(id, user.getName(), user.getEmail()),
                jwtService.sign(id));
    }
}
