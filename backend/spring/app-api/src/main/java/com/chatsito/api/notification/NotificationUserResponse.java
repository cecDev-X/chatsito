package com.chatsito.api.notification;

public record NotificationUserResponse(String name, String avatar) {
    static NotificationUserResponse from(NotificationUserDocument user) {
        return new NotificationUserResponse(user.getName(), user.getAvatar());
    }
}
