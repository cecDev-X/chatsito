package com.chatsito.api.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserDeleteService {
    private final MongoTemplate mongoTemplate;

    public UserDeleteService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public boolean delete(String id) {
        try {
            var user = mongoTemplate.findById(new ObjectId(id), UserDocument.class);
            if (user == null) {
                return false;
            }
            return mongoTemplate.remove(user).getDeletedCount() > 0;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
