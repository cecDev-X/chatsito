package com.chatsito.api.post;

import java.util.ArrayList;

import com.chatsito.api.notification.NotificationCreationService;
import com.chatsito.api.user.UserDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostLikeService {
    private final MongoTemplate mongoTemplate;
    private final NotificationCreationService notificationCreationService;

    public PostLikeService(
            MongoTemplate mongoTemplate,
            NotificationCreationService notificationCreationService) {
        this.mongoTemplate = mongoTemplate;
        this.notificationCreationService = notificationCreationService;
    }

    public PostResponse toggle(String id, String userId) {
        try {
            var post = mongoTemplate.findById(new ObjectId(id), PostDocument.class);
            if (post == null) {
                return null;
            }

            var likes = new ArrayList<>(post.getLikes());
            if (!likes.remove(userId)) {
                var actor = mongoTemplate.findById(new ObjectId(userId), UserDocument.class);
                if (actor == null) {
                    return null;
                }
                likes.add(userId);
                String details = "user " + actor.getName() + " Like On Your Post";
                notificationCreationService.create(details, post.getCreator(), id, actor);
            }
            post.setLikes(likes);
            return PostResponse.from(mongoTemplate.save(post));
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
