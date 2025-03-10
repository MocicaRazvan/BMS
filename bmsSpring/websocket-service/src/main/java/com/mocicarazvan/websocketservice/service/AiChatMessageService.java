package com.mocicarazvan.websocketservice.service;

import com.mocicarazvan.websocketservice.dtos.ai.AiChatMessagePayload;
import com.mocicarazvan.websocketservice.dtos.ai.AiChatMessageResponse;

import java.util.List;

public interface AiChatMessageService {


    AiChatMessageResponse addMessage(AiChatMessagePayload aiChatMessagePayload);

    void deleteAllMessagesByUserEmail(String email);

    List<AiChatMessageResponse> getMessagesByUserEmail(String email);

    List<AiChatMessageResponse> addMessageBulk(List<AiChatMessagePayload> payload);

    void deleteByVercelIdAndUser(String vercelId, String email);
}
