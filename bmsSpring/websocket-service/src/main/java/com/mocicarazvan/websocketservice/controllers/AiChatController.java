package com.mocicarazvan.websocketservice.controllers;


import com.mocicarazvan.websocketservice.dtos.ai.AiChatMessagePayload;
import com.mocicarazvan.websocketservice.dtos.ai.AiChatMessageResponse;
import com.mocicarazvan.websocketservice.service.AiChatMessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    public ResponseEntity<List<AiChatMessageResponse>> getMessagesByUserEmail(@Valid @NotBlank @PathVariable String userEmail) {
        return ResponseEntity.ok(aiChatMessageService.getMessagesByUserEmail(userEmail));
    }

    @MessageMapping("/ai-chat/addMessage")
    public void addMessage(@Valid @Payload AiChatMessagePayload payload) {
        aiChatMessageService.addMessage(payload);
    }

    @MessageMapping("/ai-chat/addMessageBulk")
    public void addMessageBulk(@Valid @Payload List<AiChatMessagePayload> payload) {
        aiChatMessageService.addMessageBulk(payload);
    }

    @MessageMapping("/ai-chat/deleteAllMessagesByUserEmail/{userEmail}")
    public void deleteAllMessagesByUserEmail(@Valid @NotBlank @DestinationVariable String userEmail) {
        aiChatMessageService.deleteAllMessagesByUserEmail(userEmail);
    }

    @MessageMapping("/ai-chat/deleteAllByVercelId/{vercelId}/{userEmail}")
    public void deleteAllByVercelId(@Valid @NotBlank @DestinationVariable String vercelId,
                                    @Valid @NotBlank @DestinationVariable String userEmail) {
        aiChatMessageService.deleteByVercelIdAndUser(vercelId, userEmail);
    }


}
