package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.models.AiChatRoom;
import com.mocicarazvan.websocketservice.repositories.generic.IdGeneratedRepository;

import java.util.List;

public interface AiChatRoomRepository extends IdGeneratedRepository<AiChatRoom> {

    List<AiChatRoom> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);

}
