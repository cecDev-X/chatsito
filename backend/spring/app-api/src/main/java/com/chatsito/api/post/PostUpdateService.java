package com.chatsito.api.post;

import java.util.Objects;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostUpdateService {
    private final MongoTemplate mongoTemplate;

    public PostUpdateService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public PostUpdateResult update(String id, String userId, CreatePostRequest request) {
        try {
            var post = mongoTemplate.findById(new ObjectId(id), PostDocument.class);
            if (post == null) {
                return PostUpdateResult.failure(PostUpdateResult.Status.NOT_FOUND);
            }
            if (!Objects.equals(post.getCreator(), userId)) {
                return PostUpdateResult.failure(PostUpdateResult.Status.NOT_AUTHORIZED);
            }

            post.setTitle(request.title());
            post.setMessage(request.message());
            post.setSelectedFile(request.selectedFile());
            return PostUpdateResult.success(PostResponse.from(mongoTemplate.save(post)));
        } catch (RuntimeException exception) {
            return PostUpdateResult.failure(PostUpdateResult.Status.FAILED);
        }
    }
}
