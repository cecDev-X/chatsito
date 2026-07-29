package com.chatsito.api.notification;

import java.time.Instant;

import com.chatsito.compat.LegacyFields;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "Notification")
public class NotificationDocument {
    @Id
    private ObjectId id;
    @Field(LegacyFields.NOTIFICATION_DETAILS)
    private String details;
    private String mainuid;
    private String targetid;
    @Field(LegacyFields.NOTIFICATION_READ)
    private boolean read;
    private Instant createdAt;
    private NotificationUserDocument user;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getMainuid() {
        return mainuid;
    }

    public void setMainuid(String mainuid) {
        this.mainuid = mainuid;
    }

    public String getTargetid() {
        return targetid;
    }

    public void setTargetid(String targetid) {
        this.targetid = targetid;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public NotificationUserDocument getUser() {
        return user;
    }

    public void setUser(NotificationUserDocument user) {
        this.user = user;
    }
}
