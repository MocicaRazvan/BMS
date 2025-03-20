package com.mocicarazvan.websocketservice.service.impl;

import com.mocicarazvan.websocketservice.annotations.CustomRetryable;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomUserDto;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserPayload;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserResponse;
import com.mocicarazvan.websocketservice.dtos.user.JoinedConversationUser;
import com.mocicarazvan.websocketservice.enums.ConnectedStatus;
import com.mocicarazvan.websocketservice.exceptions.notFound.ConversationUserNotFound;
import com.mocicarazvan.websocketservice.exceptions.notFound.NoChatRoomFound;
import com.mocicarazvan.websocketservice.exceptions.notFound.ReactiveUserNotFound;
import com.mocicarazvan.websocketservice.mappers.ConversationUserMapper;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.repositories.ChatRoomRepository;
import com.mocicarazvan.websocketservice.repositories.ConversationUserRepository;
import com.mocicarazvan.websocketservice.rpc.UserRPCClient;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationUserServiceImpl implements ConversationUserService {
    private final ConversationUserMapper conversationUserMapper;
    private final ConversationUserRepository conversationUserRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final CustomConvertAndSendToUser customConvertAndSendToUser;
    private final UserRPCClient userRPCClient;
    private final SimpleAsyncTaskExecutor asyncExecutor;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public ConversationUserResponse addUser(ConversationUserPayload conversationUserPayload) {
        checkReactiveUserExistence(conversationUserPayload.getEmail());
        return conversationUserRepository.findByEmail(conversationUserPayload.getEmail())
                .map(cur -> conversationUserMapper.copyFromPayload(conversationUserPayload, cur))
                .map(conversationUserRepository::save)
                .map(this::mapToResponseAndNotify)
                .orElseGet(() ->
                        conversationUserRepository.save(conversationUserMapper.fromPayloadToModel(conversationUserPayload))
                                .map(this::mapToResponseAndNotify)
                );
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public ConversationUserResponse changeUserConnectedStatus(ConnectedStatus connectedStatus, String email) {
        return conversationUserRepository.findByEmail(email)
                .map(cur -> {
                    cur.setConnectedStatus(connectedStatus);
                    if (connectedStatus == ConnectedStatus.OFFLINE)
                        cur.setConnectedChatRoom(null);
                    return cur;
                }).map(conversationUserRepository::save)
                .map(this::mapToResponseAndNotify)
                .orElseGet(() -> addUser(ConversationUserPayload.builder().email(email)
                        .connectedStatus(connectedStatus).build()));
    }

    @Override
    public ConversationUser getUserByEmail(String email) {
        return conversationUserRepository.findByEmail(email)
                .orElseThrow(() -> new ConversationUserNotFound(email));
    }

    @Override
    @Transactional
    public ConversationUser getOrCreateUserByEmail(String email) {
        return conversationUserRepository.findByEmail(email)
                .orElseGet(() -> saveUser(ConversationUser.builder().email(email)
                        .connectedChatRoom(null)
                        .connectedStatus(ConnectedStatus.ONLINE)
                        .build()));
    }

    @Override
    @CustomRetryable
    public CompletableFuture<ConversationUser> getUserByEmailAsync(String email) {
        return getUserByEmail(email).
                map(CompletableFuture::completedFuture);
    }

    @Override
    @CustomRetryable
    public ConversationUser saveUser(ConversationUser conversationUser) {
        checkReactiveUserExistence(conversationUser.getEmail());
        return
                conversationUserRepository.findByEmail(conversationUser.getEmail())
                        .orElseGet(() ->
                                conversationUserRepository.save(conversationUser)
                        );
    }

    @Override
    @CustomRetryable
    public ConversationUser saveUserByEmailIfNotExist(String email) {
        checkReactiveUserExistence(email);
        return conversationUserRepository.findByEmail(email)
                .orElseGet(() -> conversationUserRepository.save(ConversationUser.builder().email(email)
                        .connectedStatus(ConnectedStatus.OFFLINE)
                        .build()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public List<ConversationUserResponse> getConnectedUsers() {
        return conversationUserRepository.findAllByConnectedStatusIs(ConnectedStatus.ONLINE)
                .stream()
                .map(conversationUserMapper::fromModelToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public ConversationUserResponse changeUserChatRoom(ChatRoomUserDto chatRoomUserDto) {
        return getUserByEmail(chatRoomUserDto.getUserEmail())
                .map(u -> {
                    if (chatRoomUserDto.getChatId() == null) {
                        u.setConnectedChatRoom(null);
                        return u;
                    } else {
                        return chatRoomRepository.findById(chatRoomUserDto.getChatId())
                                .orElseThrow(() -> new NoChatRoomFound(chatRoomUserDto.getChatId()))
                                .map(c -> {
                                    u.setConnectedChatRoom(c);
                                    u.setConnectedStatus(ConnectedStatus.ONLINE);
                                    return u;
                                });
                    }
                }).map(conversationUserRepository::save)
                .map(this::mapToResponseAndNotify);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public ConversationUserResponse mapToResponseAndNotify(ConversationUser conversationUser) {
        notifyOtherUsers(conversationUser.getEmail());
        customConvertAndSendToUser.sendToUser(conversationUser.getEmail(), "/queue/chat-changed",
                ConversationUserResponse.builder()
                        .id(conversationUser.getId())
                        .connectedChatRoom(conversationUser.getConnectedChatRoom() == null ? null :
                                ChatRoomResponse.builder()
                                        .id(conversationUser.getConnectedChatRoom().getId())
                                        .users(conversationUser.getConnectedChatRoom().getUsers()
                                                .stream()
                                                .map(conversationUserMapper::fromModelToResponse)
                                                .collect(Collectors.toSet()))
                                        .build())
                        .build());
        return conversationUserMapper.fromModelToResponse(conversationUser);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public void notifyOtherUsers(String senderEmail) {
        chatRoomRepository.findOthersEmailsBySenderEmail(senderEmail)
                .forEach(d -> customConvertAndSendToUser.sendToUser(d.getUserEmail(), "/queue/chatRooms",
                        ChatRoomResponse.builder()
                                .id(d.getChatId())
                                .users(
                                        Stream.of(d.getUserEmail(), senderEmail)
                                                .map(this::getUserByEmail)
                                                .map(conversationUserMapper::fromModelToResponse)
                                                .collect(Collectors.toSet())
                                )
                                .build()));
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    @Override
    public CompletableFuture<List<JoinedConversationUser>> getJoinedConnectedConversationUsers() {
        return fromConversationToJoinedUsers(getConnectedUsers());
    }

    @Override
    public CompletableFuture<List<JoinedConversationUser>> fromConversationToJoinedUsers(Collection<ConversationUserResponse> conversationUserResponses) {
        Map<String, ConversationUserResponse> userResponseMap = Set.copyOf(conversationUserResponses).stream()
                .collect(Collectors.toMap(ConversationUserResponse::getEmail, Function.identity(),
                        (a, b) -> a
                ));
        return userRPCClient.getUsersByEmails(userResponseMap.keySet())
                .thenApplyAsync(reactiveUserDtos -> reactiveUserDtos
                        .stream()
                        .map(ru -> JoinedConversationUser.builder()
                                .reactiveUser(ru)
                                .conversationUser(userResponseMap.get(ru.getEmail()))
                                .build())
                        .collect(Collectors.toList()), asyncExecutor);
    }

    private void checkReactiveUserExistence(String email) {
        if (!userRPCClient.existsUserByEmail(email).join()) {
            throw new ReactiveUserNotFound(email);
        }
    }


}
