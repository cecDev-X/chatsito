package com.chatsito.api.chat;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chatsito.api.config.LegacyValidationExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ChatSendControllerTest {
    private ChatSendService sendService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        sendService = Mockito.mock(ChatSendService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ChatSendController(sendService))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void returnsLegacyMessageEnvelope() throws Exception {
        var request = new SendMessageRequest("hello", "sender", "receiver");
        var response = new SentMessageResponse(
                new ChatMessageResponse("message-id", "hello", "sender", "receiver"));
        when(sendService.send(request)).thenReturn(response);

        performPost("{\"content\":\"hello\",\"sender\":\"sender\",\"recever\":\"receiver\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg._id").value("message-id"))
                .andExpect(jsonPath("$.msg.recever").value("receiver"));
    }

    @Test
    void returnsUnprocessableEntityForMissingLegacyField() throws Exception {
        performPost("{\"content\":\"hello\",\"sender\":\"sender\"}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail[0].loc[1]").value("recever"));
    }

    @Test
    void returnsLegacyNullWhenMessageWriteFails() throws Exception {
        var request = new SendMessageRequest("hello", "sender", "receiver");
        when(sendService.send(request)).thenReturn(null);

        performPost("{\"content\":\"hello\",\"sender\":\"sender\",\"recever\":\"receiver\"}")
                .andExpect(status().isOk())
                .andExpect(content().string("null"));
    }

    private org.springframework.test.web.servlet.ResultActions performPost(String body) throws Exception {
        return mockMvc.perform(post("/chat/sendmessage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
