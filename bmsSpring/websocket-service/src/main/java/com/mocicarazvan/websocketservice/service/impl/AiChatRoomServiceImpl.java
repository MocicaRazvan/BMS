package com.mocicarazvan.websocketservice.service.impl;

import com.mocicarazvan.websocketservice.models.AiChatRoom;
import com.mocicarazvan.websocketservice.repositories.AiChatRoomRepository;
import com.mocicarazvan.websocketservice.service.AiChatRoomService;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AiChatRoomServiceImpl implements AiChatRoomService {

    private final ConversationUserService conversationUserService;
    private final AiChatRoomRepository aiChatRoomRepository;

    @Override
    public AiChatRoom getOrCreateChatRoomByEmail(String email) {
        return conversationUserService.getOrCreateUserByEmail(email)
                .map(user -> aiChatRoomRepository.findAllByUserId(user.getId())
                        .stream().findFirst()
                        .orElseGet(() -> aiChatRoomRepository.save(AiChatRoom.builder()
                                .user(user)
                                .build()))
                );
    }

    @Override
    public void deleteChatRoomByUserEmail(String email) {
        aiChatRoomRepository.deleteAllByUserId(conversationUserService.getUserByEmail(email).getId());
    }
}
