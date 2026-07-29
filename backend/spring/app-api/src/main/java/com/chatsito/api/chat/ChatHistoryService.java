package com.chatsito.api.chat;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ChatHistoryService {
    static final int PAGE_SIZE = 8;

    private final MongoTemplate mongoTemplate;

    public ChatHistoryService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public ChatHistoryResponse getHistory(int page, String firstUserId, String secondUserId) {
        var conversation = new Criteria().orOperator(
                Criteria.where("sender").is(firstUserId).and("recever").is(secondUserId),
                Criteria.where("sender").is(secondUserId).and("recever").is(firstUserId));

        long total = mongoTemplate.count(Query.query(conversation), MessageDocument.class);
        var pageQuery = Query.query(conversation)
                .with(Sort.by(Sort.Direction.DESC, "_id"))
                .skip((long) page * PAGE_SIZE)
                .limit(PAGE_SIZE);

        List<MessageDocument> messages = mongoTemplate.find(pageQuery, MessageDocument.class);
        Collections.reverse(messages);

        List<ChatMessageResponse> responseMessages = messages.stream()
                .map(ChatMessageResponse::from)
                .toList();
        boolean hasMore = (long) (page + 1) * PAGE_SIZE < total;
        return new ChatHistoryResponse(responseMessages, hasMore);
    }
}
