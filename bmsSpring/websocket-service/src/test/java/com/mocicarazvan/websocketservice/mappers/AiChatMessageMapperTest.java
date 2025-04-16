package com.mocicarazvan.websocketservice.mappers;

import com.mocicarazvan.websocketservice.dtos.ai.AiChatMessagePayload;
import com.mocicarazvan.websocketservice.dtos.ai.AiChatMessageResponse;
import com.mocicarazvan.websocketservice.enums.AiChatRole;
import com.mocicarazvan.websocketservice.models.AiChatMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AiChatMessageMapperTest {

    private final AiChatMessageMapper mapper = new AiChatMessageMapper();

    @DisplayName("fromModelToResponse should map all fields correctly")
    @Test
    void fromModelToResponseMapsAllFieldsCorrectly() {
        AiChatMessage model = AiChatMessage.builder()
                .id(1L)
                .role(AiChatRole.USER)
                .content("Hello")
                .vercelId("vercel123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        AiChatMessageResponse response = mapper.fromModelToResponse(model);

        assertEquals(model.getId(), response.getId());
        assertEquals(model.getRole(), response.getRole());
        assertEquals(model.getContent(), response.getContent());
        assertEquals(model.getVercelId(), response.getVercelId());
        assertEquals(model.getCreatedAt(), response.getCreatedAt());
        assertEquals(model.getUpdatedAt(), response.getUpdatedAt());
    }

    @DisplayName("fromModelToResponse should handle null model gracefully")
    @Test
    void fromModelToResponseHandlesNullModelGracefully() {
        assertThrows(NullPointerException.class, () -> mapper.fromModelToResponse(null));

    }

    @DisplayName("fromPayloadToModel should map all fields correctly")
    @Test
    void fromPayloadToModelMapsAllFieldsCorrectly() {
        AiChatMessagePayload payload = AiChatMessagePayload.builder()
                .role(AiChatRole.USER)
                .content("Hi there")
                .vercelId("vercel456")
                .build();

        AiChatMessage model = mapper.fromPayloadToModel(payload);

        assertEquals(payload.getRole(), model.getRole());
        assertEquals(payload.getContent(), model.getContent());
        assertEquals(payload.getVercelId(), model.getVercelId());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getUpdatedAt());
    }

    @DisplayName("fromPayloadToModel should handle null payload gracefully")
    @Test
    void fromPayloadToModelHandlesNullPayloadGracefully() {
        assertThrows(NullPointerException.class, () -> mapper.fromPayloadToModel(null));

    }
}