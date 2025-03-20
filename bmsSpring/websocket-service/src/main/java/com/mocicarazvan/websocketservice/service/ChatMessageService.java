package com.mocicarazvan.websocketservice.service;

import com.mocicarazvan.websocketservice.dtos.PageableResponse;
import com.mocicarazvan.websocketservice.dtos.message.ChatMessagePayload;
import com.mocicarazvan.websocketservice.dtos.message.ChatMessageResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface ChatMessageService {
    ChatMessageResponse sendMessage(ChatMessagePayload chatMessagePayload);

    PageableResponse<List<ChatMessageResponse>> getMessages(Long chatRoomId, int offset, int limit);

    void sendTyping(@Valid ChatMessagePayload chatMessagePayload);
}
