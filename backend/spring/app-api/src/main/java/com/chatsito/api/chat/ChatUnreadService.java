package com.chatsito.api.chat;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ChatUnreadService {
    private final MongoTemplate mongoTemplate;

    public ChatUnreadService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public UnreadSummaryResponse getUnread(String userId) {
        var query = Query.query(Criteria.where("mainUserid").is(userId)
                .and("isReaded").is(false));
        var messages = mongoTemplate.find(query, UnreadMessageDocument.class).stream()
                .map(UnreadMessageResponse::from)
                .toList();
        int total = messages.stream()
                .mapToInt(UnreadMessageResponse::numOfUnreadedMessages)
                .sum();
        return new UnreadSummaryResponse(messages, total);
    }

    public MarkReadResponse markRead(String mainUserId, String otherUserId) {
        var pairQuery = Query.query(Criteria.where("mainUserid").is(mainUserId)
                .and("otherUserid").is(otherUserId));
        var existing = mongoTemplate.findOne(pairQuery, UnreadMessageDocument.class);
        if (existing == null) {
            return new MarkReadResponse(false);
        }

        var update = new Update()
                .set("isReaded", true)
                .set("numOfUnreadedMessages", 0);
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(existing.getId())),
                update,
                UnreadMessageDocument.class);
        return new MarkReadResponse(true);
    }
}
