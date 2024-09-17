package com.mocicarazvan.websocketservice.service;

import com.mocicarazvan.websocketservice.models.AiChatRoom;

public interface AiChatRoomService {

    AiChatRoom getOrCreateChatRoomByEmail(String email);

    void deleteChatRoomByUserEmail(String email);


}
