package com.chatsito.api.notification;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.chatsito.api.config.LegacyValidationExceptionHandler;

class NotificationQueryControllerTest {
    private NotificationQueryService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(NotificationQueryService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new NotificationQueryController(service))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void returnsLegacyNotificationPayload() throws Exception {
        var notification = new NotificationResponse(
                "notification-id",
                "user Actor Start Following You",
                "main-user",
                "target-id",
                false,
                Instant.parse("2026-07-28T12:00:00Z"),
                new NotificationUserResponse("Actor", null));
        when(service.getNotifications("main-user"))
                .thenReturn(new NotificationListResponse(List.of(notification)));

        mockMvc.perform(get("/notification/main-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications[0]._id").value("notification-id"))
                .andExpect(jsonPath("$.notifications[0].deatils")
                        .value("user Actor Start Following You"))
                .andExpect(jsonPath("$.notifications[0].isreded").value(false))
                .andExpect(jsonPath("$.notifications[0].user.name").value("Actor"));
    }

    @Test
    void marksNotificationsAndReturnsLegacyMessage() throws Exception {
        mockMvc.perform(get("/notification/mark-notification-asreaded")
                        .queryParam("id", "main-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification maked as read"));

        verify(service).markAllRead("main-user");
    }

    @Test
    void usesValidationStatusWhenMarkReadIdIsMissing() throws Exception {
        mockMvc.perform(get("/notification/mark-notification-asreaded"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail[0].loc[1]").value("id"));
    }
}
