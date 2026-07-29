package com.chatsito.api.post;

import java.util.List;
import java.util.regex.Pattern;

import com.chatsito.api.user.UserDocument;
import com.chatsito.api.user.UserResponse;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class PostSearchService {
    private final MongoTemplate mongoTemplate;

    public PostSearchService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public SearchResponse search(String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return new SearchResponse(new SearchDataResponse(List.of(), List.of()));
        }

        var pattern = Pattern.compile(searchQuery, Pattern.CASE_INSENSITIVE);
        var postQuery = Query.query(new Criteria().orOperator(
                Criteria.where("title").regex(pattern),
                Criteria.where("message").regex(pattern)));
        var userQuery = Query.query(new Criteria().orOperator(
                Criteria.where("name").regex(pattern),
                Criteria.where("email").regex(pattern)));

        var posts = mongoTemplate.find(postQuery, PostDocument.class).stream()
                .map(PostResponse::from)
                .toList();
        var users = mongoTemplate.find(userQuery, UserDocument.class).stream()
                .map(UserResponse::from)
                .toList();
        return new SearchResponse(new SearchDataResponse(users, posts));
    }
}
