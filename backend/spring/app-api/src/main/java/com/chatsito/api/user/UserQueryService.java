package com.chatsito.api.user;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class UserQueryService {
    private static final int SUGGESTION_LIMIT = 10;

    private final MongoTemplate mongoTemplate;

    public UserQueryService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public UserProfileResponse getProfile(String userId) {
        try {
            var user = mongoTemplate.findById(new ObjectId(userId), UserDocument.class);
            return user == null ? null : new UserProfileResponse(UserResponse.from(user), "posts");
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public UserSuggestionsResponse getSuggestions(String userId) {
        try {
            var mainUser = mongoTemplate.findById(new ObjectId(userId), UserDocument.class);
            if (mainUser == null) {
                return new UserSuggestionsResponse(List.of());
            }

            var excludedIds = new ArrayList<ObjectId>();
            excludedIds.add(mainUser.getId());
            addObjectIds(excludedIds, mainUser.getFollowing());
            addObjectIds(excludedIds, mainUser.getFollowers());

            var query = Query.query(Criteria.where("_id").nin(excludedIds))
                    .limit(SUGGESTION_LIMIT);
            var users = mongoTemplate.find(query, UserDocument.class).stream()
                    .map(UserResponse::from)
                    .toList();
            return new UserSuggestionsResponse(users);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private void addObjectIds(List<ObjectId> destination, List<String> ids) {
        ids.stream()
                .filter(id -> id != null && !id.isEmpty())
                .map(ObjectId::new)
                .forEach(destination::add);
    }
}
