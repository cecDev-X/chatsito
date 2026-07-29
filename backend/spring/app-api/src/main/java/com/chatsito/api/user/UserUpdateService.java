package com.chatsito.api.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserUpdateService {
    private final MongoTemplate mongoTemplate;

    public UserUpdateService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public UserProfileResponse update(String id, UpdateUserRequest request) {
        try {
            var user = mongoTemplate.findById(new ObjectId(id), UserDocument.class);
            if (user == null) {
                return null;
            }
            user.setName(request.name());
            user.setBio(request.bio());
            user.setImageUrl(request.imageUrl());
            var saved = mongoTemplate.save(user);
            return new UserProfileResponse(UserResponse.from(saved), "posts");
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
