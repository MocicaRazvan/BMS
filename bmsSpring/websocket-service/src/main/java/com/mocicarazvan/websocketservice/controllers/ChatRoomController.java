package com.mocicarazvan.websocketservice.controllers;

import com.mocicarazvan.websocketservice.dtos.PageableBody;
import com.mocicarazvan.websocketservice.dtos.PageableResponse;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomPayload;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponseJoined;
import com.mocicarazvan.websocketservice.dtos.chatRoom.DeleteChatRoomRequest;
import com.mocicarazvan.websocketservice.service.ChatRoomService;
import com.mocicarazvan.websocketservice.utils.EmailNormalizerWrapperHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @MessageMapping("/addChatRoom")
    public void addChatRoom(@Valid @Payload ChatRoomPayload chatRoomPayload) {
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
        return ResponseEntity.ok(chatRoomService.getChatRoomsFiltered(email, EmailNormalizerWrapperHolder.EmailNormalizer.normalize(filterReceiver), pageableBody));
    }

    @PatchMapping("/chatRooms/filter-joined/{email}")
    public ResponseEntity<PageableResponse<List<ChatRoomResponseJoined>>> getChatRoomsFilterJoined(@PathVariable String email,
                                                                                                   @RequestParam(required = false, defaultValue = "") String filterReceiver,
                                                                                                   @Valid @RequestBody PageableBody pageableBody) {
        return ResponseEntity.ok(chatRoomService.getChatRoomsFilteredJoined(email, EmailNormalizerWrapperHolder.EmailNormalizer.normalize(filterReceiver), pageableBody));
    }

    @DeleteMapping("/chatRooms")
    public ResponseEntity<Void> deleteChatRoom(@Valid @RequestBody DeleteChatRoomRequest deleteChatRoomRequest) {
        chatRoomService.deleteChatRoom(deleteChatRoomRequest.getChatRoomId(), deleteChatRoomRequest.getSenderEmail());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/chatRooms/findAllByEmails")
    public ResponseEntity<ChatRoomResponse> findAllByEmails(@RequestParam List<String> emails) {
        return ResponseEntity.ok(chatRoomService.findAllByEmails(EmailNormalizerWrapperHolder.EmailNormalizer.normalize(emails)));
    }

    @GetMapping("/chatRooms/findAllByEmails-joined")
    public ResponseEntity<ChatRoomResponseJoined> findAllByEmailsJoined(@RequestParam List<String> emails) {
        return ResponseEntity.ok(chatRoomService.findAllByEmailsJoined(EmailNormalizerWrapperHolder.EmailNormalizer.normalize(emails)));
    }

}
