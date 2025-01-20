package com.mocicarazvan.websocketservice.controllers;


import com.mocicarazvan.websocketservice.dtos.PageableResponse;
import com.mocicarazvan.websocketservice.dtos.message.ChatMessagePayload;
import com.mocicarazvan.websocketservice.dtos.message.ChatMessageResponse;
import com.mocicarazvan.websocketservice.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload ChatMessagePayload chatMessagePayload) {
        chatMessageService.sendMessage(chatMessagePayload);
    }

    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<PageableResponse<List<ChatMessageResponse>>> getMessages(@PathVariable Long chatRoomId,
                                                                                   @RequestParam int offset,
                                                                                   @RequestParam int limit
    ) {
        return ResponseEntity.ok(chatMessageService.getMessages(chatRoomId, offset, limit));
    }

}
