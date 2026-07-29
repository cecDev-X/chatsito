package com.chatsito.api.user;

import java.util.ArrayList;

import com.chatsito.api.notification.NotificationCreationService;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserFollowService {
    private final MongoTemplate mongoTemplate;
    private final NotificationCreationService notificationCreationService;

    public UserFollowService(
            MongoTemplate mongoTemplate,
            NotificationCreationService notificationCreationService) {
        this.mongoTemplate = mongoTemplate;
        this.notificationCreationService = notificationCreationService;
    }

    public UserFollowResponse toggle(String targetId, String actorId) {
        try {
            var target = mongoTemplate.findById(new ObjectId(targetId), UserDocument.class);
            var actor = mongoTemplate.findById(new ObjectId(actorId), UserDocument.class);
            if (target == null || actor == null) {
                return null;
            }

            var followers = new ArrayList<>(target.getFollowers());
            var following = new ArrayList<>(actor.getFollowing());
            if (followers.remove(actorId)) {
                following.remove(targetId);
            } else {
                followers.add(actorId);
                following.add(targetId);
                String details = "user " + actor.getName() + " Start Following You";
                notificationCreationService.create(details, targetId, actorId, actor);
            }
            target.setFollowers(followers);
            actor.setFollowing(following);

            var savedTarget = mongoTemplate.save(target);
            var savedActor = mongoTemplate.save(actor);
            return new UserFollowResponse(
                    UserResponse.from(savedTarget),
                    UserResponse.from(savedActor));
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
