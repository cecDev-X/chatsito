package com.chatsito.api.notification;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NotificationResponse(
        @JsonProperty("_id") String id,
        @JsonProperty("deatils") String details,
        String mainuid,
        String targetid,
        @JsonProperty("isreded") boolean read,
        Instant createdAt,
        NotificationUserResponse user) {
    static NotificationResponse from(NotificationDocument notification) {
        return new NotificationResponse(
                notification.getId().toHexString(),
                notification.getDetails(),
                notification.getMainuid(),
                notification.getTargetid(),
                notification.isRead(),
                notification.getCreatedAt(),
                NotificationUserResponse.from(notification.getUser()));
    }
}
