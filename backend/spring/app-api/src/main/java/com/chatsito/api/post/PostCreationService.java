package com.chatsito.api.post;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostCreationService {
    private final MongoTemplate mongoTemplate;

    public PostCreationService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public PostResponse create(CreatePostRequest request, String userId) {
        try {
            var post = new PostDocument();
            post.setMessage(request.message());
            post.setSelectedFile(request.selectedFile());
            post.setTitle(request.title());
            post.setCreator(userId);
            post.setLikes(List.of());
            post.setCreatedAt(Instant.now());
            return PostResponse.from(mongoTemplate.save(post));
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
