package com.mocicarazvan.websocketservice.service.impl;

import com.mocicarazvan.websocketservice.annotations.CustomRetryable;
import com.mocicarazvan.websocketservice.dtos.PageableBody;
import com.mocicarazvan.websocketservice.dtos.PageableResponse;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomPayload;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponseJoined;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomUserDto;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserBase;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserResponse;
import com.mocicarazvan.websocketservice.dtos.user.JoinedConversationUser;
import com.mocicarazvan.websocketservice.exceptions.MoreThenOneChatRoom;
import com.mocicarazvan.websocketservice.exceptions.SameUserChatRoom;
import com.mocicarazvan.websocketservice.exceptions.UserIsConnectedToTheRoom;
import com.mocicarazvan.websocketservice.exceptions.notFound.NoChatRoomFound;
import com.mocicarazvan.websocketservice.mappers.ChatRoomMapper;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import com.mocicarazvan.websocketservice.models.ChatRoom;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.repositories.ChatMessageRepository;
import com.mocicarazvan.websocketservice.repositories.ChatRoomRepository;
import com.mocicarazvan.websocketservice.service.ChatMessageNotificationService;
import com.mocicarazvan.websocketservice.service.ChatRoomService;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import com.mocicarazvan.websocketservice.utils.PageableUtilsCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMapper chatRoomMapper;
    private final ConversationUserService conversationUserService;
    private final CustomConvertAndSendToUser customConvertAndSendToUser;
    private final SimpleAsyncTaskExecutor asyncExecutor;
    private final ChatMessageNotificationService chatMessageNotificationService;
    private final ChatMessageRepository chatMessageRepository;
    private final PageableUtilsCustom pageableUtilsCustom;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public ChatRoomResponse createChatRoom(ChatRoomPayload chatRoomPayload) {
        Set<String> emails = chatRoomPayload.getUsers()
                .stream().map(ConversationUserBase::getEmail)
                .collect(Collectors.toSet());

        if (emails.size() == 1) {
            throw new SameUserChatRoom();
        }
//        emails.forEach(conversationUserService::saveUserByEmailIfNotExist);
        CompletableFuture.allOf(
                emails.stream()
                        .map(e -> CompletableFuture.runAsync(
                                () -> conversationUserService.saveUserByEmailIfNotExist(e),
                                asyncExecutor
                        ))
                        .toArray(CompletableFuture[]::new)
        ).join();


        List<ChatRoom> rooms = getRoomsByUsers(emails);
        if (rooms.size() > 1) {
            throw new MoreThenOneChatRoom(emails);
        }
        if (rooms.isEmpty()) {
            return chatRoomMapper.fromPayloadToModel(chatRoomPayload)
                    .map(chatRoomRepository::save)
                    .map(c -> {
                        ChatRoomResponse cr = chatRoomMapper.fromModelToResponse(c);
                        notifyUsers(chatRoomPayload.getUsers(), cr, "-create");
                        return cr;
                    });
        }
        return chatRoomMapper.fromModelToResponse(rooms.getFirst())
                .map(cr -> {
                    notifyUsers(chatRoomPayload.getUsers(), cr);
                    return cr;
                });
    }


    @Override
    public List<ChatRoom> getRoomsByUsers(Set<String> emails) {
        return chatRoomRepository.findByUsers(emails, emails.size());
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public List<ChatRoomResponse> getChatRooms(String email) {
        return chatRoomRepository.findChatRoomsByUserEmail(email)
                .stream()
                .map(chatRoomMapper::fromModelToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public PageableResponse<List<ChatRoomResponse>> getChatRoomsFiltered(String email, String filterEmail, PageableBody pageableBody) {


        PageRequest pageRequest = pageableUtilsCustom.createPageRequest(pageableBody);

        if (!pageRequest.getSort().isSorted() && filterEmail != null && !filterEmail.isEmpty()) {
            pageRequest = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(),
                    Sort.by(Sort.Order.desc("email_similarity"))
            );
        }

        // findFilteredChatRooms asta fu inainte
        return
                pageableUtilsCustom.createPageableResponse(
                        chatRoomRepository.findFilteredChatRoomsWithSimilarity(email, filterEmail, pageRequest),
                        chatRoomMapper::fromModelToResponse);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    @Override
    public void deleteChatRoom(Long id, String senderEmail) {
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
                                chatMessageNotificationService.notifyDeleteByReferenceId(id, receiverEmails),
                                deleteChatRoomById(id)
                        ).thenRunAsync(() -> {

                            notifyUsersModel(
                                    new HashSet<>(chatRoom.getUsers()),
                                    chatRoomMapper.fromModelToResponse(chatRoom),
                                    "-delete"
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
        if (rooms.isEmpty()) {
            throw new NoChatRoomFound(emails);
        }
        return chatRoomMapper.fromModelToResponse(rooms.get(0));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public PageableResponse<List<ChatRoomResponseJoined>> getChatRoomsFilteredJoined(String email, String filterReceiver, PageableBody pageableBody) {
        PageableResponse<List<ChatRoomResponse>> chatRoomsFiltered = getChatRoomsFiltered(email, filterReceiver, pageableBody);
        List<ChatRoomResponse> chatRooms = chatRoomsFiltered.getContent();
        List<ChatRoomResponseJoined> responseList = fromChatRoomResponseToJoined(chatRooms);
        return new PageableResponse<>(responseList, chatRoomsFiltered.getPageInfo(), chatRoomsFiltered.getLinks());
    }

    private List<ChatRoomResponseJoined> fromChatRoomResponseToJoined(List<ChatRoomResponse> chatRooms) {
        List<ConversationUserResponse> allUsers = chatRooms.stream()
                .flatMap(chatRoom -> chatRoom.getUsers().stream())
                .collect(Collectors.toList());
        CompletableFuture<Map<String, JoinedConversationUser>> joinedUsersFuture = conversationUserService
                .fromConversationToJoinedUsers(allUsers)
                .thenApply(joinedUsers -> joinedUsers.stream()
                        .collect(Collectors.toMap(
                                user -> user.getConversationUser().getEmail(),
                                user -> user,
                                (u1, u2) -> u1
                        )));

        return joinedUsersFuture.thenApplyAsync(joinedUsersMap -> chatRooms.stream()
                .map(chatRoom -> {
                    Set<JoinedConversationUser> joinedUsers = chatRoom.getUsers().stream()
                            .map(user -> joinedUsersMap.get(user.getEmail()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    return new ChatRoomResponseJoined(chatRoom, joinedUsers);
                })
                .collect(Collectors.toList()), asyncExecutor).join();
    }


    @Override
    public ChatRoomResponseJoined findAllByEmailsJoined(List<String> emails) {
        return
                findAllByEmails(emails)
                        .map(chatRoomResponse -> new ChatRoomResponseJoined(chatRoomResponse, new HashSet<>(conversationUserService.fromConversationToJoinedUsers(chatRoomResponse.getUsers())
                                .join())));
    }


    public ChatRoom getChatRoomById(Long id) {
        return chatRoomRepository.findById(id).orElseThrow(() -> new NoChatRoomFound(id));
    }


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
//        users.forEach(u -> customConvertAndSendToUser.sendToUser(u.getEmail(), "/queue/chatRooms" + path, chatRoomResponse));
        CompletableFuture<Void> sendToQueueFuture = CompletableFuture.runAsync(() ->
                users.forEach(u -> customConvertAndSendToUser.sendToUser(
                        u.getEmail(), "/queue/chatRooms" + path, chatRoomResponse
                )), asyncExecutor
        );

        ChatRoomResponseJoined chatRoomResponseJoined = fromChatRoomResponseToJoined(List.of(chatRoomResponse)).get(0);

        CompletableFuture<Void> sendToJoinedQueueFuture = CompletableFuture.runAsync(() -> {
            if (chatRoomResponseJoined != null) {
                users.forEach(u -> customConvertAndSendToUser.sendToUser(
                        u.getEmail(), "/queue/chatRooms-joined" + path, chatRoomResponseJoined
                ));
            }
        }, asyncExecutor);

        CompletableFuture.allOf(sendToQueueFuture, sendToJoinedQueueFuture).join();
    }


    public void notifyUsersModel(Set<ConversationUser> users, ChatRoomResponse chatRoomResponse, String path) {
//        users.forEach(u -> customConvertAndSendToUser.sendToUser(u.getEmail(), "/queue/chatRooms" + path, chatRoomResponse));
        CompletableFuture<Void> sendToQueueFuture = CompletableFuture.runAsync(() ->
                users.forEach(u -> customConvertAndSendToUser.sendToUser(
                        u.getEmail(), "/queue/chatRooms" + path, chatRoomResponse
                )), asyncExecutor
        );

        ChatRoomResponseJoined chatRoomResponseJoined = fromChatRoomResponseToJoined(List.of(chatRoomResponse)).get(0);

        CompletableFuture<Void> sendToJoinedQueueFuture = CompletableFuture.runAsync(() -> {
            if (chatRoomResponseJoined != null) {
                users.forEach(u -> customConvertAndSendToUser.sendToUser(
                        u.getEmail(), "/queue/chatRooms-joined" + path, chatRoomResponseJoined
                ));
            }
        }, asyncExecutor);

        CompletableFuture.allOf(sendToQueueFuture, sendToJoinedQueueFuture).join();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public void notifyOtherUsersRoomChange(String senderEmail, Function<ChatRoomUserDto, ChatRoomResponse> createRoom) {
        CompletableFuture.allOf(chatRoomRepository.findOthersEmailsBySenderEmail(senderEmail)
                .stream().map(d -> CompletableFuture.runAsync(() -> {
                    ChatRoomResponse chatRoomResponse = createRoom.apply(d);
                    customConvertAndSendToUser.sendToUser(d.getUserEmail(), "/queue/chatRooms",
                            chatRoomResponse);
                    ChatRoomResponseJoined chatRoomResponseJoined = fromChatRoomResponseToJoined(List.of(chatRoomResponse)).get(0);
                    customConvertAndSendToUser.sendToUser(d.getUserEmail(), "/queue/chatRooms-joined",
                            chatRoomResponseJoined);
                }))
                .toArray(CompletableFuture[]::new)
        ).join();

    }


}
