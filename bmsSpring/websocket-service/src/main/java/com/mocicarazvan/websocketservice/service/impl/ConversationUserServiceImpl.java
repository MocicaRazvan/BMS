package com.mocicarazvan.websocketservice.service.impl;

import com.mocicarazvan.websocketservice.annotations.CustomRetryable;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomUserDto;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserPayload;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserResponse;
import com.mocicarazvan.websocketservice.enums.ConnectedStatus;
import com.mocicarazvan.websocketservice.exceptions.notFound.ConversationUserNotFound;
import com.mocicarazvan.websocketservice.exceptions.notFound.NoChatRoomFound;
import com.mocicarazvan.websocketservice.mappers.ConversationUserMapper;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.repositories.ChatRoomRepository;
import com.mocicarazvan.websocketservice.repositories.ConversationUserRepository;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    @CustomRetryable
    public ConversationUserResponse addUser(ConversationUserPayload conversationUserPayload) {
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
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CustomRetryable
    public ConversationUserResponse changeUserConnectedStatus(ConnectedStatus connectedStatus, String email) {
        return conversationUserRepository.findByEmail(email)
                .map(cur -> {
                    cur.setConnectedStatus(connectedStatus);
//                    cur.setConnectedChatRoom(null);
                    if (connectedStatus == ConnectedStatus.OFFLINE)
                        cur.setConnectedChatRoom(null);
                    return cur;
                }).map(conversationUserRepository::save)
                .map(this::mapToResponseAndNotify)
                .orElseGet(() -> addUser(ConversationUserPayload.builder().email(email)
                        .connectedStatus(connectedStatus).build()));
    }

    @Override
//    @Transactional
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    @CustomRetryable
    public ConversationUser getUserByEmail(String email) {
        return conversationUserRepository.findByEmail(email)
                .orElseThrow(() -> new ConversationUserNotFound(email));
    }

    @Override
//    @Transactional
//    @CustomRetryable
    public ConversationUser getOrCreateUserByEmail(String email) {
        return conversationUserRepository.findByEmail(email)
                .orElseGet(() -> conversationUserRepository.save(ConversationUser.builder().email(email)
                        .connectedChatRoom(null)
                        .connectedStatus(ConnectedStatus.ONLINE)
                        .build()));
    }

    @Override
//    @Transactional
//    @Transactional
//    @CustomRetryable
    public CompletableFuture<ConversationUser> getUserByEmailAsync(String email) {
        return getUserByEmail(email).
                map(CompletableFuture::completedFuture);
    }

    @Override
//    @Transactional
//    @CustomRetryable
    public ConversationUser saveUser(ConversationUser conversationUser) {
        return conversationUserRepository.save(conversationUser);
    }

    @Override
//    @Transactional
//    @CustomRetryable
    public ConversationUser saveUserByEmailIfNotExist(String email) {
        return conversationUserRepository.findByEmail(email)
                .orElseGet(() -> conversationUserRepository.save(ConversationUser.builder().email(email)
                        .connectedStatus(ConnectedStatus.OFFLINE)
                        .build()));
    }

    @Override
//    @Transactional
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    @CustomRetryable
//    @Retryable(
//            retryFor = {OptimisticLockException.class,
//                    PessimisticLockException.class,
//                    CannotAcquireLockException.class,
//                    JpaSystemException.class,
//                    LockAcquisitionException.class,
//                    ObjectOptimisticLockingFailureException.class,
//                    CannotAcquireLockException.class, SQLException.class, StaleObjectStateException.class},
//            maxAttempts = 5,
//            backoff = @Backoff(delay = 200, multiplier = 2, maxDelay = 1000))
    public List<ConversationUserResponse> getConnectedUsers() {
        return conversationUserRepository.findAllByConnectedStatusIs(ConnectedStatus.ONLINE)
                .stream()
                .map(conversationUserMapper::fromModelToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
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

    //    @Transactional
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CustomRetryable
    public ConversationUserResponse mapToResponseAndNotify(ConversationUser conversationUser) {
        notifyOtherUsers(conversationUser.getEmail());
        log.error("Conversation user chat room: {}", conversationUser.getConnectedChatRoom());
//        messagingTemplate.convertAndSendToUser(conversationUser.getEmail(), "/chat/changed",
//                ConversationUserResponse.builder()
//                        .id(conversationUser.getId())
//                        .connectedChatRoom(conversationUser.getConnectedChatRoom() == null ? null :
//                                ChatRoomResponse.builder()
//                                        .id(conversationUser.getConnectedChatRoom().getId())
//                                        .users(conversationUser.getConnectedChatRoom().getUsers()
//                                                .stream()
//                                                .map(conversationUserMapper::fromModelToResponse)
//                                                .collect(Collectors.toSet()))
//                                        .build())
//                        .build());
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

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CustomRetryable
    public void notifyOtherUsers(String senderEmail) {
//        chatRoomRepository.findOthersEmailsBySenderEmail(senderEmail)
//                .forEach(d -> messagingTemplate.convertAndSendToUser(d.getUserEmail(), "/chatRooms",
//                        ChatRoomResponse.builder()
//                                .id(d.getChatId())
//                                .users(
//                                        Stream.of(d.getUserEmail(), senderEmail)
//                                                .map(this::getUserByEmail)
//                                                .map(conversationUserMapper::fromModelToResponse)
//                                                .collect(Collectors.toSet())
//                                )
//                                .build()));
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


}
