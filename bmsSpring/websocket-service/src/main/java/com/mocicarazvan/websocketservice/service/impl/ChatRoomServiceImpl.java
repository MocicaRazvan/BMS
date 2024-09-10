package com.mocicarazvan.websocketservice.service.impl;

import com.mocicarazvan.websocketservice.annotations.CustomRetryable;
import com.mocicarazvan.websocketservice.dtos.PageableBody;
import com.mocicarazvan.websocketservice.dtos.PageableResponse;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomPayload;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserBase;
import com.mocicarazvan.websocketservice.exceptions.MoreThenOneChatRoom;
import com.mocicarazvan.websocketservice.exceptions.SameUserChatRoom;
import com.mocicarazvan.websocketservice.exceptions.UserIsConnectedToTheRoom;
import com.mocicarazvan.websocketservice.exceptions.notFound.NoChatRoomFound;
import com.mocicarazvan.websocketservice.mappers.ChatRoomMapper;
import com.mocicarazvan.websocketservice.models.ChatRoom;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.repositories.ChatMessageRepository;
import com.mocicarazvan.websocketservice.repositories.ChatRoomRepository;
import com.mocicarazvan.websocketservice.service.ChatMessageNotificationService;
import com.mocicarazvan.websocketservice.service.ChatRoomService;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import com.mocicarazvan.websocketservice.utils.PageableUtilsCustom;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMapper chatRoomMapper;
    private final ConversationUserService conversationUserService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Executor asyncExecutor;
    private final ChatMessageNotificationService chatMessageNotificationService;
    private final ChatMessageRepository chatMessageRepository;
    private final PageableUtilsCustom pageableUtilsCustom;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CustomRetryable
    public ChatRoomResponse createChatRoom(ChatRoomPayload chatRoomPayload) {
        Set<String> emails = chatRoomPayload.getUsers()
                .stream().map(ConversationUserBase::getEmail)
                .collect(Collectors.toSet());

        emails.forEach(conversationUserService::saveUserByEmailIfNotExist);

        if (emails.size() == 1) {
            throw new SameUserChatRoom();
        }

        List<ChatRoom> rooms = getRoomsByUsers(emails);
        if (rooms.size() > 1) {
            throw new MoreThenOneChatRoom(emails);
        }
        if (rooms.isEmpty()) {
            return chatRoomMapper.fromPayloadToModel(chatRoomPayload)
                    .map(chatRoomRepository::save)
                    .map(c -> {
                        ChatRoomResponse cr = chatRoomMapper.fromModelToResponse(c);
                        notifyUsers(chatRoomPayload.getUsers(), cr);
                        return cr;
                    });
        }
        return chatRoomMapper.fromModelToResponse(rooms.getFirst())
                .map(cr -> {
                    notifyUsers(chatRoomPayload.getUsers(), cr);
                    return cr;
                });
    }

    // todo scoate transactional
//    @Transactional
    @Override
    public List<ChatRoom> getRoomsByUsers(Set<String> emails) {
        var rooms = chatRoomRepository.findByUsers(emails
                , emails.size());
        log.error("Rooms: {}", rooms);
        return rooms;
    }

    // todo scoate transactional
//    @Transactional
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CustomRetryable
    public List<ChatRoomResponse> getChatRooms(String email) {
        log.error("Email: {}", email);
        var initial = chatRoomRepository.findChatRoomsByUserEmail(email);
        log.error("Initial: {}", initial.toString());
        var rooms = initial
                .stream()
                .map(chatRoomMapper::fromModelToResponse)
                .collect(Collectors.toList());
        log.error("Rooms : {}", rooms.toString());
        return rooms;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    @CustomRetryable
    public PageableResponse<List<ChatRoomResponse>> getChatRoomsFiltered(String email, String filterEmail, PageableBody pageableBody) {

        //todo remove
        var v = chatRoomRepository.findFilteredChatRooms(email, filterEmail,
                pageableUtilsCustom.createPageRequest(pageableBody));

        log.error("Email: {}", email);
        log.error("Filter email: {}", filterEmail);
        log.error("Chat rooms {} , {}", v.getTotalElements(), v.getContent());

        return
                pageableUtilsCustom.createPageableResponse(
                        chatRoomRepository.findFilteredChatRooms(email, filterEmail,
                                pageableUtilsCustom.createPageRequest(pageableBody)),
                        chatRoomMapper::fromModelToResponse);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    @CustomRetryable
    @Override
    public void deleteChatRoom(Long id, String senderEmail) {
        log.error("Delete chat room id: {} sender email: {}", id, senderEmail);
        CompletableFuture<ChatRoom> chatRoomCompletableFuture = getChatRoomByIdAsync(id);
        CompletableFuture<ConversationUser> conversationUserCompletableFuture = conversationUserService.getUserByEmailAsync(senderEmail);

        CompletableFuture.allOf(chatRoomCompletableFuture, conversationUserCompletableFuture)
                .thenComposeAsync(v -> {
                    try {
                        ChatRoom chatRoom = chatRoomCompletableFuture.get();
                        ConversationUser conversationUser = conversationUserCompletableFuture.get();
                        chatRoom.getUsers().stream()
                                .filter(u -> !Objects.equals(u.getId(), conversationUser.getId())
                                        && u.getConnectedChatRoom() != null && u.getConnectedChatRoom().getId().equals(id)
                                )
                                .findFirst().ifPresent(u -> {
                                    throw new UserIsConnectedToTheRoom(u.getId());
                                });

                        List<String> receiverEmails = chatRoom.getUsers()
                                .stream().filter(u -> !u.getId().equals(conversationUser.getId()))
                                .map(ConversationUser::getEmail)
                                .toList();

                        return CompletableFuture.allOf(
//                                deleteMessagesByChatRoomId(id),
                                chatMessageNotificationService.notifyDeleteByReferenceId(id, receiverEmails),
                                deleteChatRoomById(id)
                        ).thenRunAsync(() -> {
                            notifyUsersModel(
                                    new HashSet<>(chatRoom.getUsers()),
                                    chatRoomMapper.fromModelToResponse(chatRoom),
                                    "/delete"
                            );
                        }, asyncExecutor);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, asyncExecutor).join();
    }

    @Override
    public ChatRoomResponse findAllByEmails(List<String> emails) {
        List<ChatRoom> rooms = chatRoomRepository.findAllByUserEmails(emails, emails.size());
        if (rooms.size() > 1) {
            throw new MoreThenOneChatRoom(new HashSet<>(emails));
        }
        return chatRoomMapper.fromModelToResponse(rooms.get(0));
    }

    //    @Transactional

    public ChatRoom getChatRoomById(Long id) {
        return chatRoomRepository.findById(id).orElseThrow(() -> new NoChatRoomFound(id));
    }

    //    @Transactional

    public CompletableFuture<ChatRoom> getChatRoomByIdAsync(Long id) {
        return CompletableFuture.supplyAsync(() -> getChatRoomById(id), asyncExecutor);
    }


    public CompletableFuture<Void> deleteChatRoomById(Long id) {
        return CompletableFuture.runAsync(() -> chatRoomRepository.deleteById(id), asyncExecutor);
    }


    public CompletableFuture<Void> deleteMessagesByChatRoomId(Long chatRoomId) {
        return CompletableFuture.runAsync(() -> chatMessageRepository.deleteAllByChatRoomId(chatRoomId), asyncExecutor);
    }


    public void notifyUsers(Set<ConversationUserBase> users, ChatRoomResponse chatRoomResponse) {
        notifyUsers(users, chatRoomResponse, "");
    }


    public void notifyUsers(Set<ConversationUserBase> users, ChatRoomResponse chatRoomResponse, String path) {
        users.forEach(u -> messagingTemplate.convertAndSendToUser(u.getEmail(), "/chatRooms" + path, chatRoomResponse));
    }


    public void notifyUsersModel(Set<ConversationUser> users, ChatRoomResponse chatRoomResponse, String path) {
        users.forEach(u -> messagingTemplate.convertAndSendToUser(u.getEmail(), "/chatRooms" + path, chatRoomResponse));
    }


}
