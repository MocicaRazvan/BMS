package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.models.ChatMessage;
import com.mocicarazvan.websocketservice.repositories.generic.IdGeneratedRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatMessageRepository extends IdGeneratedRepository<ChatMessage> {

//    @Query("""
//                select cm from ChatMessage cm
//                where cm.chatRoom.id = :chatRoomId
//                order by cm.timestamp asc
//            """)
//    List<ChatMessage> findAllByChatRoomId(Long chatRoomId);

    Page<ChatMessage> findAllByChatRoomId(Long chatRoomId, Pageable pageable);

    //    @Lock(LockModeType.PESSIMISTIC_WRITE)
    void deleteAllByChatRoomId(Long chatRoomId);
}
