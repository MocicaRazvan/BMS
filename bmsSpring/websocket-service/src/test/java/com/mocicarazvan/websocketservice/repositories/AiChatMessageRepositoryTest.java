package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.config.TestContainerImages;
import com.mocicarazvan.websocketservice.enums.AiChatRole;
import com.mocicarazvan.websocketservice.enums.ConnectedStatus;
import com.mocicarazvan.websocketservice.models.AiChatMessage;
import com.mocicarazvan.websocketservice.models.AiChatRoom;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Execution(ExecutionMode.SAME_THREAD)
class AiChatMessageRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(TestContainerImages.POSTGRES_IMAGE);

    @Autowired
    private AiChatMessageRepository aiChatMessageRepository;
    @Autowired
    private ConversationUserRepository conversationUserRepository;
    @Autowired
    private AiChatRoomRepository aiChatRoomRepository;

    @BeforeEach
    void seedDatabase() {

        aiChatMessageRepository.deleteAll();
        aiChatRoomRepository.deleteAll();
        conversationUserRepository.deleteAll();
        List<ConversationUser> conversationUsers = IntStream.range(0, 2)
                .mapToObj(i -> ConversationUser.builder()
                        .email("email" + i)
                        .connectedStatus(i % 2 == 0 ? ConnectedStatus.ONLINE : ConnectedStatus.OFFLINE)
                        .build())
                .collect(Collectors.toList());
        var savedUsers = conversationUserRepository.saveAll(conversationUsers);

        List<AiChatRoom> rooms = IntStream.range(0, 2)
                .mapToObj(i -> AiChatRoom.builder()
                        .user(savedUsers.get(i))
                        .build())
                .collect(Collectors.toList());

        var savedRooms = aiChatRoomRepository.saveAll(rooms);

        List<AiChatMessage> messages = IntStream.range(0, 100)
                .mapToObj(i -> AiChatMessage.builder()
                        .content("content" + i)
                        .vercelId("vercelId" + i)
                        .role(i % 2 == 0 ? AiChatRole.USER : AiChatRole.ASSISTANT)
                        .chatRoom(savedRooms.get(i % 2))
                        .build())
                .collect(Collectors.toList());
        aiChatMessageRepository.saveAll(messages);
    }

    @Test
    @Order(1)
    void loads() {
        assertNotNull(aiChatMessageRepository);
        assertEquals(100, aiChatMessageRepository.count());
        assertEquals(2, aiChatRoomRepository.count());
        assertEquals(2, conversationUserRepository.count());
    }


    @ParameterizedTest
    @ValueSource(ints = {10, 20, 30})
    void deleteMessagesBeyondLimit_success(int limit) {
        var roomId = aiChatRoomRepository.findAll().getFirst().getId();
        aiChatMessageRepository.deleteMessagesBeyondLimit(roomId, limit);
        var actual = aiChatMessageRepository.findAllByChatRoom_Id(roomId).size();
        assertEquals(limit, actual);
    }

    @Test
    void deleteMessagesBeyondLimit_noMessages() {
        var roomId = aiChatRoomRepository.findAll().getFirst().getId();
        aiChatMessageRepository.deleteMessagesBeyondLimit(roomId, 0);
        var actual = aiChatMessageRepository.findAllByChatRoom_Id(roomId).size();
        assertEquals(0, actual);
    }


}