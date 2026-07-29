package com.chatsito.api.notification;

import java.util.regex.Pattern;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class NotificationQueryService {
    private final MongoTemplate mongoTemplate;

    public NotificationQueryService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public NotificationListResponse getNotifications(String userId) {
        var recipientPattern = Pattern.compile(userId, Pattern.CASE_INSENSITIVE);
        var query = Query.query(Criteria.where("mainuid").regex(recipientPattern))
                .with(Sort.by(Sort.Direction.DESC, "_id"));
        var notifications = mongoTemplate.find(query, NotificationDocument.class).stream()
                .map(NotificationResponse::from)
                .toList();
        return new NotificationListResponse(notifications);
    }

    public void markAllRead(String userId) {
        var query = Query.query(Criteria.where("mainuid").is(userId));
        var update = new Update().set("isreded", true);
        mongoTemplate.updateMulti(query, update, NotificationDocument.class);
    }
}
