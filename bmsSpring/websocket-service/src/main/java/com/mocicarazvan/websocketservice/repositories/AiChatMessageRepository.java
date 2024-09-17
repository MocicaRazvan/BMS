package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.models.AiChatMessage;
import com.mocicarazvan.websocketservice.repositories.generic.IdGeneratedRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AiChatMessageRepository extends IdGeneratedRepository<AiChatMessage> {

    List<AiChatMessage> findAllByChatRoomId(Long chatRoomId, Sort sort);

    void deleteAllByChatRoomId(Long chatRoomId);


    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM ai_chat_message
            WHERE chat_room_id = :chatRoomId
            AND id NOT IN (
                SELECT id FROM ai_chat_message
                WHERE chat_room_id = :chatRoomId
                ORDER BY created_at DESC
                LIMIT :limit
            )
            """, nativeQuery = true)
    void deleteMessagesBeyondLimit(@Param("chatRoomId") Long chatRoomId, @Param("limit") int limit);
}
