package com.chatsito.api.chat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatUnreadController {
    private final ChatUnreadService chatUnreadService;

    public ChatUnreadController(ChatUnreadService chatUnreadService) {
        this.chatUnreadService = chatUnreadService;
    }

    @GetMapping("/get-user-unreadedmsg")
    public UnreadSummaryResponse getUnread(@RequestParam String userid) {
        return chatUnreadService.getUnread(userid);
    }

    @GetMapping("/mark-msg-asreaded")
    public MarkReadResponse markRead(
            @RequestParam String mainuid,
            @RequestParam String otheruid) {
        return chatUnreadService.markRead(mainuid, otheruid);
    }
}
