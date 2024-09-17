package com.mocicarazvan.websocketservice.mappers;


import com.mocicarazvan.websocketservice.dtos.ai.AiChatMessagePayload;
import com.mocicarazvan.websocketservice.dtos.ai.AiChatMessageResponse;
import com.mocicarazvan.websocketservice.mappers.generic.ModelResponseMapper;
import com.mocicarazvan.websocketservice.models.AiChatMessage;
import org.springframework.stereotype.Component;

@Component
public class AiChatMessageMapper implements ModelResponseMapper<AiChatMessage, AiChatMessageResponse> {
    @Override
    public AiChatMessageResponse fromModelToResponse(AiChatMessage aiChatMessage) {
        return AiChatMessageResponse.builder()
                .role(aiChatMessage.getRole())
                .content(aiChatMessage.getContent())
                .vercelId(aiChatMessage.getVercelId())
                .id(aiChatMessage.getId())
                .createdAt(aiChatMessage.getCreatedAt())
                .updatedAt(aiChatMessage.getUpdatedAt())
                .build();
    }

    public AiChatMessage fromPayloadToModel(AiChatMessagePayload aiChatMessagePayload) {
        return AiChatMessage.builder()
                .content(aiChatMessagePayload.getContent())
                .vercelId(aiChatMessagePayload.getVercelId())
                .role(aiChatMessagePayload.getRole())
                .build();
    }
}
