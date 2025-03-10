package com.mocicarazvan.websocketservice.controllers;


import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomUserDto;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserPayload;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserResponse;
import com.mocicarazvan.websocketservice.enums.ConnectedStatus;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ConversationUserController {
    private final ConversationUserService conversationUserService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/addUser")
    public void addUser(@Valid @Payload ConversationUserPayload conversationUserPayload) {
        conversationUserService.addUser(conversationUserPayload)
                .map(cur ->
                {
                    simpMessagingTemplate.convertAndSend("/chat/connected",
                            cur);
                    return null;
                });

    }

    @MessageMapping("/connectUser/{email}")
    public void connectUser(@DestinationVariable String email) {
        conversationUserService.changeUserConnectedStatus(ConnectedStatus.ONLINE, email)
                .map(cur -> {
                    simpMessagingTemplate.convertAndSend("/chat/connected",
                            cur);
                    return null;
                });
    }

    @MessageMapping("/disconnectUser/{email}")
    public void disconnectUser(@DestinationVariable String email) {
//        log.error("Disconnect user: {}", email);
        conversationUserService.changeUserConnectedStatus(ConnectedStatus.OFFLINE, email)
                .map(cur -> {
                    simpMessagingTemplate.convertAndSend("/chat/connected",
                            cur);
                    return null;
                });
    }

    @GetMapping("/getConnectedUsers")
    public ResponseEntity<List<ConversationUserResponse>> getConnectedUsers() {
        return ResponseEntity.ok(conversationUserService.getConnectedUsers());
    }

    @MessageMapping("/changeRoom")
    public void changeRoom(@Valid @Payload ChatRoomUserDto chatRoomUserDto) {
//        log.error("Change room: {}", chatRoomUserDto);
        conversationUserService.changeUserChatRoom(chatRoomUserDto)
                .map(cur -> {
                    simpMessagingTemplate.convertAndSend("/chat/connected",
                            cur);
                    return null;
                });
    }


}
