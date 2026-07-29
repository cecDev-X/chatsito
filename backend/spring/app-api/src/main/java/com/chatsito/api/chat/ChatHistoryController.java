package com.chatsito.api.chat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatHistoryController {
    private final ChatHistoryService chatHistoryService;

    public ChatHistoryController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    @GetMapping("/getmsgsbynums")
    public ChatHistoryResponse getMessages(
            @RequestParam(name = "from", defaultValue = "0") int page,
            @RequestParam String firstuid,
            @RequestParam String seconduid) {
        return chatHistoryService.getHistory(page, firstuid, seconduid);
    }
}
