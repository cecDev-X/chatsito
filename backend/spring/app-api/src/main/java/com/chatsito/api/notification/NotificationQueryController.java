package com.chatsito.api.notification;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
public class NotificationQueryController {
    private final NotificationQueryService notificationQueryService;

    public NotificationQueryController(NotificationQueryService notificationQueryService) {
        this.notificationQueryService = notificationQueryService;
    }

    @GetMapping("/mark-notification-asreaded")
    public Map<String, String> markRead(@RequestParam String id) {
        notificationQueryService.markAllRead(id);
        return Map.of("message", "Notification maked as read");
    }

    @GetMapping("/{userid}")
    public NotificationListResponse getNotifications(@PathVariable String userid) {
        return notificationQueryService.getNotifications(userid);
    }
}
