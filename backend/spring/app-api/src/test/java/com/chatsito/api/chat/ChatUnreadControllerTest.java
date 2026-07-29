package com.chatsito.api.chat;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.chatsito.api.config.LegacyValidationExceptionHandler;

class ChatUnreadControllerTest {
    private ChatUnreadService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(ChatUnreadService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatUnreadController(service))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void returnsLegacyUnreadEnvelopeAndFieldNames() throws Exception {
        var message = new UnreadMessageResponse(
                "record-id", "main-user", "sender-a", 3, false);
        when(service.getUnread("main-user"))
                .thenReturn(new UnreadSummaryResponse(List.of(message), 3));

        mockMvc.perform(get("/chat/get-user-unreadedmsg")
                        .queryParam("userid", "main-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].otherUserid").value("sender-a"))
                .andExpect(jsonPath("$.messages[0].numOfUnreadedMessages").value(3))
                .andExpect(jsonPath("$.messages[0].isReaded").value(false))
                .andExpect(jsonPath("$.total").value(3));
    }

    @Test
    void returnsMarkResult() throws Exception {
        when(service.markRead("main-user", "sender-a"))
                .thenReturn(new MarkReadResponse(true));

        mockMvc.perform(get("/chat/mark-msg-asreaded")
                        .queryParam("mainuid", "main-user")
                        .queryParam("otheruid", "sender-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isMarked").value(true));
    }

    @Test
    void returnsValidationStatusForMissingUserId() throws Exception {
        mockMvc.perform(get("/chat/get-user-unreadedmsg"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail[0].loc[1]").value("userid"));
    }
}
