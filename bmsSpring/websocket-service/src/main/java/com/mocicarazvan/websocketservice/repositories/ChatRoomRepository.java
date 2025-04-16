package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomUserDto;
import com.mocicarazvan.websocketservice.models.ChatRoom;
import com.mocicarazvan.websocketservice.repositories.generic.IdGeneratedRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ChatRoomRepository extends IdGeneratedRepository<ChatRoom> {
    @Query("""
                select cr from ChatRoom cr
                join cr.users u
                where u.email in :userEmail
                group by cr.id
                having count(u.id) = :count and count(u.id) > 0
            """)
    List<ChatRoom> findByUsers(Set<String> userEmail, long count);

    @Query("""
            select new com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomUserDto(cr.id,u.email)  from ChatRoom cr
            join cr.users u
            where u.email != :senderEmail
            and cr.id in (
                select cr2.id from ChatRoom cr2
                join cr2.users u2
                where u2.email = :senderEmail
                )
            """)
    List<ChatRoomUserDto> findOthersEmailsBySenderEmail(String senderEmail);

    @Query("SELECT cr FROM ChatRoom cr JOIN cr.users u WHERE u.email = :userEmail")
    List<ChatRoom> findChatRoomsByUserEmail(String userEmail);

    List<ChatRoom> findAllByUsersEmail(String email);

    @Query("SELECT cr FROM ChatRoom cr JOIN cr.users u WHERE u.email IN :emails GROUP BY cr HAVING COUNT(DISTINCT u.email) = :emailCount")
    List<ChatRoom> findAllByUserEmails(Collection<String> emails, long emailCount);

    //    @Query("""
//            SELECT DISTINCT cr FROM ChatRoom cr JOIN cr.users u
//            WHERE LOWER( u.email) LIKE LOWER(CONCAT('%', :filterEmail, '%')) AND cr.id NOT IN (
//            SELECT cr2.id FROM ChatRoom cr2 JOIN cr2.users u2 WHERE u2.email = :email)
//            """)
    //todo change, this works for 2 users in a room
//    @Query("""
//            SELECT DISTINCT cr FROM ChatRoom cr JOIN cr.users u
//            WHERE LOWER( u.email) LIKE LOWER(CONCAT('%', :filterEmail, '%')) AND LOWER( u.email) != LOWER(:email)
//            """)
//    @Query("""
//                SELECT cr FROM ChatRoom cr
//                WHERE cr.id IN (
//                    SELECT cr_inner.id
//                    FROM ChatRoom cr_inner
//                    JOIN cr_inner.users u_inner
//                    GROUP BY cr_inner.id
//                    HAVING COUNT(DISTINCT u_inner.id) = 2
//                      AND SUM(CASE WHEN LOWER(u_inner.email) = LOWER(:email) THEN 1 ELSE 0 END) > 0
//                      AND SUM(CASE WHEN LOWER(u_inner.email) LIKE LOWER(CONCAT('%', :filterEmail, '%')) THEN 1 ELSE 0 END) > 0
//                )
//            """)
//    Page<ChatRoom> findFilteredChatRooms(String email, String filterEmail, Pageable pageable);

    @Query("""
                SELECT cr FROM ChatRoom cr
                WHERE cr.id IN (
                    SELECT cr_inner.id
                    FROM ChatRoom cr_inner
                    JOIN cr_inner.users u_inner
                    GROUP BY cr_inner.id
                    HAVING COUNT(DISTINCT u_inner.id) = 2
                      AND SUM(CASE WHEN LOWER(u_inner.email) = LOWER(:email) THEN 1 ELSE 0 END) > 0
                      AND SUM(CASE WHEN
                          LOWER(u_inner.email) LIKE LOWER(CONCAT('%', :filterEmail, '%'))
                          AND LOWER(u_inner.email)!=LOWER(:email)
                       THEN 1 ELSE 0 END) > 0
                )
            """)
    Page<ChatRoom> findFilteredChatRooms(String email, String filterEmail, Pageable pageable);

    @Query(value = """
                SELECT * FROM (
                    SELECT cr.*,
                        similarity(
                            (SELECT LOWER(u.email)
                             FROM chat_room_users cru
                             JOIN conversation_user u ON cru.user_id = u.id
                             WHERE cru.chat_room_id = cr.id AND u.email != :email
                             LIMIT 1),
                            LOWER(:filterEmail)
                        ) AS email_similarity
                    FROM chat_room cr
                    WHERE cr.id IN (
                        SELECT cru.chat_room_id
                        FROM chat_room_users cru
                        JOIN conversation_user u ON cru.user_id = u.id
                        GROUP BY cru.chat_room_id
                        HAVING COUNT(DISTINCT u.id) = 2
                            AND COUNT(*) FILTER (WHERE LOWER(u.email) = LOWER(:email)) > 0
                            AND COUNT(*) FILTER (WHERE
                                u.email != :email AND (
                                    u.email ILIKE CONCAT('%', :filterEmail, '%')
                                    OR similarity(LOWER(u.email), LOWER(:filterEmail)) > 0.3
                                )
                            ) > 0
                    )
                ) AS subquery
            """,
            countQuery = """
                        SELECT COUNT(*) FROM (
                            SELECT cr.*
                            FROM chat_room cr
                            WHERE cr.id IN (
                                SELECT cru.chat_room_id
                                FROM chat_room_users cru
                                JOIN conversation_user u ON cru.user_id = u.id
                                GROUP BY cru.chat_room_id
                                HAVING COUNT(DISTINCT u.id) = 2
                                    AND COUNT(*) FILTER (WHERE LOWER(u.email) = LOWER(:email)) > 0
                                    AND COUNT(*) FILTER (WHERE
                                        u.email != :email AND (
                                            u.email ILIKE CONCAT('%', :filterEmail, '%')
                                            OR similarity(LOWER(u.email), LOWER(:filterEmail)) > 0.3
                                        )
                                    ) > 0
                            )
                        ) AS count_subquery
                    """,
            nativeQuery = true)
    Page<ChatRoom> findFilteredChatRoomsWithSimilarity(String email, String filterEmail, Pageable pageable);

}
