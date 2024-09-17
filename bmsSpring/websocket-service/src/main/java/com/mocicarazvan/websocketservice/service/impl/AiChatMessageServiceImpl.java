package com.mocicarazvan.websocketservice.service.impl;

import com.mocicarazvan.websocketservice.dtos.ai.AiChatMessagePayload;
import com.mocicarazvan.websocketservice.dtos.ai.AiChatMessageResponse;
import com.mocicarazvan.websocketservice.mappers.AiChatMessageMapper;
import com.mocicarazvan.websocketservice.models.AiChatMessage;
import com.mocicarazvan.websocketservice.repositories.AiChatMessageRepository;
import com.mocicarazvan.websocketservice.service.AiChatMessageService;
import com.mocicarazvan.websocketservice.service.AiChatRoomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AiChatMessageServiceImpl implements AiChatMessageService {

    private final AiChatRoomService aiChatRoomService;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final AiChatMessageRepository aiChatMessageRepository;

    @Value("${ai.chat.limit:20}")
    private int aiChatLimit;

    @Override
    public AiChatMessageResponse addMessage(AiChatMessagePayload aiChatMessagePayload) {
        return aiChatRoomService.getOrCreateChatRoomByEmail(aiChatMessagePayload.getEmail())
                .map(aiChatRoom -> aiChatMessageMapper.fromPayloadToModel(aiChatMessagePayload)
                        .map(aiChatMessage -> {
                            aiChatMessage.setChatRoom(aiChatRoom);
                            AiChatMessage saved = aiChatMessageRepository.save(aiChatMessage);
                            aiChatMessageRepository.deleteMessagesBeyondLimit(aiChatRoom.getId(), aiChatLimit);
                            return saved;
                        })
                        .map(aiChatMessageMapper::fromModelToResponse)
                );
    }

    @Override
    @Transactional
    public void deleteAllMessagesByUserEmail(String email) {
        aiChatRoomService.getOrCreateChatRoomByEmail(email)
                .map(aiChatRoom -> {
                    aiChatMessageRepository.deleteAllByChatRoomId(aiChatRoom.getId());
                    return null;
                });

    }

    @Override
    public List<AiChatMessageResponse> getMessagesByUserEmail(String email) {
        return aiChatRoomService.getOrCreateChatRoomByEmail(email)
                .map(aiChatRoom -> aiChatMessageRepository.findAllByChatRoomId(aiChatRoom.getId(),
                                Sort.by(Sort.Order.desc("createdAt")))
                        .stream()
                        .map(aiChatMessageMapper::fromModelToResponse)
                        .toList()
                );
    }
}
