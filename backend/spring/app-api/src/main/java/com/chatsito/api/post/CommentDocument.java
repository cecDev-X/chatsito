package com.chatsito.api.post;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Comment")
public class CommentDocument {
    @Id
    private ObjectId id;
    private String postId;
    private String userId;
    private String value;
    private Instant createdAt;

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
