package com.chatsito.api.post;

import com.chatsito.api.user.UserDocument;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class PostDetailsService {
    private final MongoTemplate mongoTemplate;

    public PostDetailsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public SinglePostResponse getPost(String id) {
        try {
            var post = mongoTemplate.findById(new ObjectId(id), PostDocument.class);
            if (post == null) {
                return null;
            }

            var commentQuery = Query.query(Criteria.where("postId").is(id))
                    .with(Sort.by(Sort.Direction.DESC, "createdAt"));
            var comments = mongoTemplate.find(commentQuery, CommentDocument.class).stream()
                    .map(this::toComment)
                    .toList();
            var details = new PostDetailsResponse(
                    post.getId().toHexString(),
                    post.getTitle(),
                    post.getMessage(),
                    post.getCreator(),
                    post.getSelectedFile(),
                    post.getLikes(),
                    post.getCreatedAt(),
                    comments);
            return new SinglePostResponse(details);
        } catch (RuntimeException exception) {
            return null;
        }
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
