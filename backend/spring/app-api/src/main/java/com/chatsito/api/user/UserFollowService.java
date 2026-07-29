package com.chatsito.api.user;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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
        if (targetId.equals(actorId)) {
            return null;
        }
        try {
            var target = mongoTemplate.findById(new ObjectId(targetId), UserDocument.class);
            var actor = mongoTemplate.findById(new ObjectId(actorId), UserDocument.class);
            if (target == null || actor == null) {
                return null;
            }

            var followers = new LinkedHashSet<>(
                    target.getFollowers() == null ? List.of() : target.getFollowers());
            var following = new LinkedHashSet<>(
                    actor.getFollowing() == null ? List.of() : actor.getFollowing());
            followers.remove(targetId);
            following.remove(actorId);
            if (followers.remove(actorId)) {
                following.remove(targetId);
            } else {
                followers.add(actorId);
                following.add(targetId);
                String details = "user " + actor.getName() + " Start Following You";
                notificationCreationService.create(details, targetId, actorId, actor);
            }
            target.setFollowers(new ArrayList<>(followers));
            actor.setFollowing(new ArrayList<>(following));

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
