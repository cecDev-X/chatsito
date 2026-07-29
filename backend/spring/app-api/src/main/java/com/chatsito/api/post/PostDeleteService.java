package com.chatsito.api.post;

import java.util.Objects;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostDeleteService {
    private final MongoTemplate mongoTemplate;

    public PostDeleteService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public PostDeleteResult delete(String id, String userId) {
        try {
            var post = mongoTemplate.findById(new ObjectId(id), PostDocument.class);
            if (post == null) {
                return PostDeleteResult.NOT_FOUND;
            }
            if (!Objects.equals(post.getCreator(), userId)) {
                return PostDeleteResult.NOT_AUTHORIZED;
            }

            var result = mongoTemplate.remove(post);
            return result.getDeletedCount() > 0
                    ? PostDeleteResult.SUCCESS
                    : PostDeleteResult.FAILED;
        } catch (RuntimeException exception) {
            return PostDeleteResult.FAILED;
        }
    }
}
