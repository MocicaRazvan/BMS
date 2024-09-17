package com.mocicarazvan.websocketservice.controllers;


import com.mocicarazvan.websocketservice.dtos.ai.AiChatMessagePayload;
import com.mocicarazvan.websocketservice.dtos.ai.AiChatMessageResponse;
import com.mocicarazvan.websocketservice.service.AiChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatMessageService aiChatMessageService;

    @GetMapping("/ai-chat/{userEmail}")
    public ResponseEntity<List<AiChatMessageResponse>> getMessagesByUserEmail(@PathVariable String userEmail) {
        return ResponseEntity.ok(aiChatMessageService.getMessagesByUserEmail(userEmail));
    }

    @MessageMapping("/ai-chat/addMessage")
    public void addMessage(@Payload AiChatMessagePayload payload) {
        aiChatMessageService.addMessage(payload);
    }

    @MessageMapping("/ai-chat/deleteAllMessagesByUserEmail/{userEmail}")
    public void deleteAllMessagesByUserEmail(@DestinationVariable String userEmail) {
        aiChatMessageService.deleteAllMessagesByUserEmail(userEmail);
    }


}
