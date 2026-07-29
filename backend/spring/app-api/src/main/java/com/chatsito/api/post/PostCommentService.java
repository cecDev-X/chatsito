package com.chatsito.api.post;

import java.time.Instant;

import com.chatsito.api.notification.NotificationCreationService;
import com.chatsito.api.user.UserDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostCommentService {
    private final MongoTemplate mongoTemplate;
    private final NotificationCreationService notificationCreationService;
    private final PostDetailsService postDetailsService;

    public PostCommentService(
            MongoTemplate mongoTemplate,
            NotificationCreationService notificationCreationService,
            PostDetailsService postDetailsService) {
        this.mongoTemplate = mongoTemplate;
        this.notificationCreationService = notificationCreationService;
        this.postDetailsService = postDetailsService;
    }

    public SinglePostResponse create(String postId, String userId, String value) {
        try {
            if (value == null) {
                return null;
            }
            var comment = new CommentDocument();
            comment.setPostId(postId);
            comment.setUserId(userId);
            comment.setValue(value);
            comment.setCreatedAt(Instant.now());
            mongoTemplate.save(comment);

            var post = mongoTemplate.findById(new ObjectId(postId), PostDocument.class);
            var actor = mongoTemplate.findById(new ObjectId(userId), UserDocument.class);
            if (post == null || actor == null) {
                return null;
            }

            String details = "user " + actor.getName() + " Comment On Your Post";
            notificationCreationService.create(details, post.getCreator(), postId, actor);
            return postDetailsService.getPost(postId);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
