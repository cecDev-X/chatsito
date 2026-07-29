package com.chatsito.api.chat;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ChatSendService {
    private final MongoTemplate mongoTemplate;

    public ChatSendService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public SentMessageResponse send(SendMessageRequest request) {
        MessageDocument saved;
        try {
            var message = new MessageDocument();
            message.setContent(request.content());
            message.setSender(request.sender());
            message.setReceiver(request.recever());
            saved = mongoTemplate.save(message);
        } catch (RuntimeException exception) {
            return null;
        }

        try {
            var query = Query.query(Criteria.where("mainUserid").is(request.recever())
                    .and("otherUserid").is(request.sender()));
            var update = new Update()
                    .inc("numOfUnreadedMessages", 1)
                    .set("isReaded", false);
            mongoTemplate.findAndModify(
                    query,
                    update,
                    FindAndModifyOptions.options().upsert(true).returnNew(true),
                    UnreadMessageDocument.class);
        } catch (RuntimeException exception) {
            // Legacy behavior returns the saved message even if the unread counter fails.
        }
        return new SentMessageResponse(ChatMessageResponse.from(saved));
    }
}
