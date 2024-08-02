package com.mocicarazvan.websocketservice.controllers;

import com.mocicarazvan.websocketservice.dtos.PageableBody;
import com.mocicarazvan.websocketservice.dtos.PageableResponse;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomPayload;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.chatRoom.DeleteChatRoomRequest;
import com.mocicarazvan.websocketservice.service.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@Controller
@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    // todo sort order users and conversation like project sem1

    private final ChatRoomService chatRoomService;

    @MessageMapping("/addChatRoom")
    public void addChatRoom(@Payload ChatRoomPayload chatRoomPayload) {
        chatRoomService.createChatRoom(chatRoomPayload);
    }

    @GetMapping("/chatRooms/{email}")
    public ResponseEntity<List<ChatRoomResponse>> getChatRooms(@PathVariable String email) {
        return ResponseEntity.ok(chatRoomService.getChatRooms(email));
    }

    @PatchMapping("/chatRooms/filter/{email}")
    public ResponseEntity<PageableResponse<List<ChatRoomResponse>>> getChatRoomsFilter(@PathVariable String email,
                                                                                       @RequestParam(required = false, defaultValue = "") String filterReceiver,
                                                                                       @Valid @RequestBody PageableBody pageableBody) {
        return ResponseEntity.ok(chatRoomService.getChatRoomsFiltered(email, filterReceiver, pageableBody));
    }

    @DeleteMapping("/chatRooms")
    public ResponseEntity<Void> deleteChatRoom(@Valid @RequestBody DeleteChatRoomRequest deleteChatRoomRequest) {
        chatRoomService.deleteChatRoom(deleteChatRoomRequest.getChatRoomId(), deleteChatRoomRequest.getSenderEmail());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/chatRooms/findAllByEmails")
    public ResponseEntity<ChatRoomResponse> findAllByEmails(@RequestParam List<String> emails) {
        return ResponseEntity.ok(chatRoomService.findAllByEmails(emails));
    }

}
