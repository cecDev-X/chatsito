package com.chatsito.api.post;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommentDeleteService {
    private final MongoTemplate mongoTemplate;

    public CommentDeleteService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public boolean delete(String id) {
        try {
            var comment = mongoTemplate.findById(new ObjectId(id), CommentDocument.class);
            if (comment == null) {
                return false;
            }
            return mongoTemplate.remove(comment).getDeletedCount() > 0;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
