package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.config.TestContainerImages;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomUserDto;
import com.mocicarazvan.websocketservice.enums.ConnectedStatus;
import com.mocicarazvan.websocketservice.models.ChatRoom;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.testUtils.PgTrgmSimilarity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Execution(ExecutionMode.SAME_THREAD)
class ChatRoomRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(TestContainerImages.POSTGRES_IMAGE)
            .withInitScript("schema-test.sql");
    @Autowired
    private ConversationUserRepository conversationUserRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int userCnt = 4;

    private static final List<String> userEmails =
            IntStream.range(0, userCnt)
                    .mapToObj(i -> "user" + i + "@gmail.com")
                    .toList();
    private static final String outsiderEmail = "outsider@gmail.com";


    @BeforeEach
    void seedDatabase() {
        chatRoomRepository.deleteAll();
        conversationUserRepository.deleteAll();


        var users = IntStream.range(0, userCnt)
                .mapToObj(i -> ConversationUser.builder()
                        .email(userEmails.get(i))
                        .connectedStatus(i % 2 == 0 ? ConnectedStatus.ONLINE : ConnectedStatus.OFFLINE)
                        .build())
                .collect(Collectors.toList());
        var savedUsers = conversationUserRepository.saveAll(users);
        // 1 room for each combination of users
        var rooms = savedUsers.stream()
                .flatMap(user1 -> savedUsers.stream()
                        .filter(user2 -> user2.getId() > user1.getId())
                        .map(user2 -> chatRoomRepository.save(ChatRoom.builder()
                                .users(Stream.of(user1, user2).collect(Collectors.toList()))
                                .build())))
                .collect(Collectors.toList());
        chatRoomRepository.saveAll(rooms);

        var outsiderUser = ConversationUser.builder()
                .email(outsiderEmail)
                .connectedStatus(ConnectedStatus.OFFLINE)
                .build();
        conversationUserRepository.save(outsiderUser);

    }

    private static Stream<String> getUserWithRoomsEmails() {
        return userEmails.stream();
    }

    private static Stream<Set<String>> getAllUserEmails() {
        return Stream.of(
                new HashSet<>(userEmails),
                Stream.concat(userEmails.stream(), Stream.of(outsiderEmail)).collect(Collectors.toSet())
        );
    }

    @Test
    @Order(1)
    void loads() {
        assertEquals(6, chatRoomRepository.findAll().size());
        assertEquals(5, conversationUserRepository.findAll().size());
    }

    @ParameterizedTest
    @MethodSource("getAllUserEmails")
    void findByUsers_allPresentEmails(Set<String> emails) {
        var actual = chatRoomRepository.findByUsers(new HashSet<>(emails), 2);
        assertEquals(6, actual.size());
    }

    private static Stream<Arguments> provideCountAndEmails() {
        return IntStream.of(1, 3, 4, 5)
                .mapToObj(i -> getAllUserEmails()
                        .map(e -> Arguments.of(i, e)))
                .flatMap(e -> e);
    }

    @ParameterizedTest
    @MethodSource("provideCountAndEmails")
    void findByUsers_invalidCount(int count, Set<String> emails) {
        var actual = chatRoomRepository.findByUsers(emails, count);
        assertEquals(0, actual.size());
    }

    @ParameterizedTest
    @MethodSource("getUserWithRoomsEmails")
    void findOthersEmailsBySenderEmail_hasRooms(String email) {
        List<ChatRoomUserDto> resp = chatRoomRepository.findOthersEmailsBySenderEmail(email);

        assertEquals(userCnt - 1, resp.size());
        assertTrue(resp.stream().noneMatch(user -> user.getUserEmail().equals(email)));

    }

    @Test
    void findOthersEmailsBySenderEmail_noRooms() {
        List<ChatRoomUserDto> resp = chatRoomRepository.findOthersEmailsBySenderEmail(outsiderEmail);

        assertEquals(0, resp.size());
    }

    @ParameterizedTest
    @MethodSource("getUserWithRoomsEmails")
    void findChatRoomsByUserEmail_hasRooms(String email) {
        List<ChatRoom> resp = chatRoomRepository.findChatRoomsByUserEmail(email);

        assertEquals(userCnt - 1, resp.size());
        assertTrue(resp.stream()
                .map(ChatRoom::getUsers)
                .allMatch(us -> us.stream().anyMatch(u -> u.getEmail().equals(email)) && us.size() == 2));
    }

    @Test
    void findChatRoomsByUserEmail_noRooms() {
        List<ChatRoom> resp = chatRoomRepository.findChatRoomsByUserEmail(outsiderEmail);

        assertEquals(0, resp.size());
    }

    @ParameterizedTest
    @MethodSource("getAllUserEmails")
    void findAllByUserEmails_presentRooms(Set<String> emails) {
        var actual = chatRoomRepository.findAllByUserEmails(emails, 2);
        assertEquals(6, actual.size());
    }

    @ParameterizedTest
    @MethodSource("provideCountAndEmails")
    void findAllByUserEmails_invalidCount(int count, Set<String> emails) {
        var actual = chatRoomRepository.findAllByUserEmails(emails, count);
        assertEquals(0, actual.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {userCnt - 1, userCnt - 2, userCnt - 3})
    void findChatRoomByUsers_foundRooms(int userInt) {
        var email = userEmails.get(userInt);
        var filterEmail = userEmails.getFirst();
        var pr = PageRequest.of(0, 10);
        Page<ChatRoom> actual = chatRoomRepository.findFilteredChatRooms(email, filterEmail, pr);
        assertEquals(1, actual.getTotalElements());
        assertEquals(1, actual.getTotalPages());
        assertEquals(1, actual.getContent().size());
        assertTrue(actual.getContent().stream()
                .allMatch(room -> room.getUsers().stream()
                        .anyMatch(user -> user.getEmail().equals(email)) &&
                        room.getUsers().stream()
                                .anyMatch(user -> user.getEmail().equals(filterEmail))));
    }

    @ParameterizedTest
    @ValueSource(ints = {userCnt - 4, userCnt - 3, userCnt - 2, userCnt - 1})
    void findChatRoomByUsers_noRooms(int userInt) {
        var email = userEmails.get(userInt);
        var pr = PageRequest.of(0, 10);
        Page<ChatRoom> actual = chatRoomRepository.findFilteredChatRooms(outsiderEmail, email, pr);
        assertEquals(0, actual.getTotalElements());
        assertEquals(0, actual.getTotalPages());
        assertEquals(0, actual.getContent().size());
        assertTrue(actual.getContent().isEmpty());
        assertTrue(actual.isEmpty());

    }

    @ParameterizedTest
    @ValueSource(ints = {userCnt - 1, userCnt - 2, userCnt - 3})
    void findFilteredChatRoomsWithSimilarity_foundRooms(int userInt) {
        var email = userEmails.get(userInt);
        var filterEmail = userEmails.getFirst();
        var similarEmails = userEmails.stream().filter(
                e -> !Objects.equals(e, email) && PgTrgmSimilarity.similarityGreaterThan(email, e, 0.3, jdbcTemplate)
        ).toList();
        var pr = PageRequest.of(0, similarEmails.size());
        Page<ChatRoom> actual = chatRoomRepository.findFilteredChatRoomsWithSimilarity(email, filterEmail, pr);
        assertEquals(similarEmails.size(), actual.getTotalElements());
        assertEquals(1, actual.getTotalPages());
        assertEquals(similarEmails.size(), actual.getContent().size());
        assertTrue(actual.getContent().stream()
                .allMatch(room ->
                        room.getUsers().size() == 2 &&
                                room.getUsers().stream().anyMatch(u -> u.getEmail().equals(email))
                                && room.getUsers().stream().anyMatch(u ->
                                similarEmails.contains(u.getEmail())
                        )
                ));
    }

    @ParameterizedTest
    @ValueSource(ints = {userCnt - 4, userCnt - 3, userCnt - 2, userCnt - 1})
    void findFilteredChatRoomsWithSimilarity_noRooms(int userInt) {
        var email = userEmails.get(userInt);
        var pr = PageRequest.of(0, 10);
        Page<ChatRoom> actual = chatRoomRepository.findFilteredChatRoomsWithSimilarity(outsiderEmail, email, pr);
        assertEquals(0, actual.getTotalElements());
        assertEquals(0, actual.getTotalPages());
        assertEquals(0, actual.getContent().size());
        assertTrue(actual.getContent().isEmpty());
        assertTrue(actual.isEmpty());

    }
}