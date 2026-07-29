package com.chatsito.api.chat;

import com.fasterxml.jackson.databind.node.NullNode;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatSendController {
    private final ChatSendService chatSendService;

    public ChatSendController(ChatSendService chatSendService) {
        this.chatSendService = chatSendService;
    }

    @PostMapping("/sendmessage")
    public ResponseEntity<?> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        var response = chatSendService.send(request);
        return ResponseEntity.ok(response == null ? NullNode.getInstance() : response);
    }
}
