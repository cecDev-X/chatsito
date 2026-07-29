package com.chatsito.api.post;

import java.util.ArrayList;
import java.util.List;

import com.chatsito.api.user.UserDocument;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class PostFeedService {
    private static final int PAGE_SIZE = 6;

    private final MongoTemplate mongoTemplate;

    public PostFeedService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public PostFeedResponse getFeed(String pageValue, String userId, String profileId) {
        try {
            int page = pageValue != null && pageValue.matches("\\d+")
                    ? Integer.parseInt(pageValue)
                    : 1;
            var criteria = feedCriteria(userId, profileId);
            var countQuery = criteria == null ? new Query() : Query.query(criteria);
            long total = mongoTemplate.count(countQuery, PostDocument.class);

            var pageQuery = criteria == null ? new Query() : Query.query(criteria);
            pageQuery.with(Sort.by(Sort.Direction.DESC, "createdAt"))
                    .skip((long) (page - 1) * PAGE_SIZE)
                    .limit(PAGE_SIZE);
            var posts = mongoTemplate.find(pageQuery, PostDocument.class).stream()
                    .map(this::enrich)
                    .toList();
            int pages = (int) Math.ceil((double) total / PAGE_SIZE);
            return new PostFeedResponse(posts, page, pages);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private Criteria feedCriteria(String userId, String profileId) {
        if (profileId != null && !profileId.isEmpty()) {
            return Criteria.where("creator").is(profileId);
        }
        if (userId == null || userId.isEmpty()) {
            return null;
        }

        var mainUser = mongoTemplate.findById(new ObjectId(userId), UserDocument.class);
        if (mainUser == null) {
            throw new IllegalArgumentException("User not found");
        }
        var creators = new ArrayList<>(mainUser.getFollowing());
        creators.add(mainUser.getId().toHexString());
        return Criteria.where("creator").in(creators);
    }

    private FeedPostResponse enrich(PostDocument post) {
        UserDocument creator = findUser(post.getCreator());
        var commentQuery = Query.query(Criteria.where("postId").is(post.getId().toHexString()))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));
        var comments = mongoTemplate.find(commentQuery, CommentDocument.class).stream()
                .map(this::toComment)
                .toList();
        return new FeedPostResponse(
                post.getId().toHexString(),
                post.getTitle(),
                post.getMessage(),
                post.getCreator(),
                post.getSelectedFile(),
                post.getLikes(),
                post.getCreatedAt(),
                creator == null ? null : creator.getName(),
                creator == null ? null : creator.getImageUrl(),
                comments);
    }

    private CommentResponse toComment(CommentDocument comment) {
        var user = findUser(comment.getUserId());
        return new CommentResponse(
                comment.getId().toHexString(),
                comment.getPostId(),
                comment.getUserId(),
                comment.getValue(),
                comment.getCreatedAt(),
                user == null ? null : CommentUserResponse.from(user));
    }

    private UserDocument findUser(String id) {
        try {
            return id == null ? null : mongoTemplate.findById(new ObjectId(id), UserDocument.class);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
