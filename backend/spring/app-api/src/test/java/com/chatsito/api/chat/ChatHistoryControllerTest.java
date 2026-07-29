package com.chatsito.api.chat;

import static org.mockito.Mockito.verify;
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

class ChatHistoryControllerTest {
    private ChatHistoryService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(ChatHistoryService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatHistoryController(service))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void preservesLegacyPathParametersAndResponseFields() throws Exception {
        var message = new ChatMessageResponse("message-id", "hello", "first-user", "second-user");
        when(service.getHistory(0, "first-user", "second-user"))
                .thenReturn(new ChatHistoryResponse(List.of(message), false));

        mockMvc.perform(get("/chat/getmsgsbynums")
                        .queryParam("from", "0")
                        .queryParam("firstuid", "first-user")
                        .queryParam("seconduid", "second-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msgs[0]._id").value("message-id"))
                .andExpect(jsonPath("$.msgs[0].recever").value("second-user"))
                .andExpect(jsonPath("$.hasMore").value(false));

        verify(service).getHistory(0, "first-user", "second-user");
    }

    @Test
    void usesValidationStatusWhenRequiredQueryParameterIsMissing() throws Exception {
        mockMvc.perform(get("/chat/getmsgsbynums")
                        .queryParam("firstuid", "first-user"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail[0].type").value("missing"))
                .andExpect(jsonPath("$.detail[0].loc[0]").value("query"))
                .andExpect(jsonPath("$.detail[0].loc[1]").value("seconduid"));
    }
}
