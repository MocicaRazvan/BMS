package com.mocicarazvan.websocketservice.service.impl;

import com.mocicarazvan.websocketservice.annotations.CustomRetryable;
import com.mocicarazvan.websocketservice.dtos.PageableResponse;
import com.mocicarazvan.websocketservice.dtos.message.ChatMessagePayload;
import com.mocicarazvan.websocketservice.dtos.message.ChatMessageResponse;
import com.mocicarazvan.websocketservice.enums.ConnectedStatus;
import com.mocicarazvan.websocketservice.exceptions.MoreThenOneChatRoom;
import com.mocicarazvan.websocketservice.exceptions.notFound.NoChatRoomFound;
import com.mocicarazvan.websocketservice.mappers.ChatMessageMapper;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import com.mocicarazvan.websocketservice.models.ChatRoom;
import com.mocicarazvan.websocketservice.repositories.ChatMessageRepository;
import com.mocicarazvan.websocketservice.service.ChatMessageService;
import com.mocicarazvan.websocketservice.service.ChatRoomService;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import com.mocicarazvan.websocketservice.utils.ChunkRequest;
import com.mocicarazvan.websocketservice.utils.PageableUtilsCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatRoomService chatRoomService;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationUserService conversationUserService;
    private final SimpleAsyncTaskExecutor asyncExecutor;
    private final PageableUtilsCustom pageableUtilsCustom;
    private final CustomConvertAndSendToUser customConvertAndSendToUser;


    @Override
//    @Transactional
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    @CustomRetryable
    public ChatMessageResponse sendMessage(ChatMessagePayload chatMessagePayload) {
        Set<String> emails = Set.of(chatMessagePayload.getSenderEmail(), chatMessagePayload.getReceiverEmail());
        List<ChatRoom> rooms = chatRoomService.getRoomsByUsers(
                Set.of(chatMessagePayload.getSenderEmail(), chatMessagePayload.getReceiverEmail())
        ).stream().filter(r -> r.getId().equals(chatMessagePayload.getChatRoomId())).toList();
        if (rooms.isEmpty()) {
            throw new NoChatRoomFound(emails);
        }
        if (rooms.size() > 1) {
            throw new MoreThenOneChatRoom(emails);
        }
        return chatMessageMapper.fromPayloadToModel(chatMessagePayload)
                .map(cm -> {
                    cm.setChatRoom(rooms.getFirst());
                    return cm;
                }).map(chatMessageRepository::save)
                .map(c -> {
//                    log.error("Chat msg payload: {}", chatMessagePayload);
                    ChatMessageResponse cmr = chatMessageMapper.fromModelToResponse(c);

                    // the front subscribes to both queues
                    // to make sure that the messages are sent for the sender also and not to make a fake ws on the front
                    // we make like this
//                    if (c.getReceiver().getConnectedChatRoom() == null ||
//                            !Objects.equals(c.getReceiver().getConnectedChatRoom().getId(), c.getChatRoom().getId())) {
//                        messagingTemplate.convertAndSendToUser(chatMessagePayload.getSenderEmail(), "/queue/messages", cmr);
//                    } else {
//                        messagingTemplate.convertAndSendToUser(chatMessagePayload.getReceiverEmail(), "/queue/messages", cmr);
//                    }

                    // todo make the check bette
                    // extra safe check just to be sure
                    // sometimes the front in dev mode navigates before the stomp publish  message is processed
//                    log.error("Sender is {} , receiver chat is {}",
//                            c.getSender().getConnectedChatRoom() != null ? c.getSender().getConnectedChatRoom().getId() : null,
//                            c.getReceiver().getConnectedChatRoom() != null ? c.getReceiver().getConnectedChatRoom().getId() : null);
                    if (c.getSender().getConnectedChatRoom() == null && c.getChatRoom() != null) {
                        c.getSender().setConnectedChatRoom(c.getChatRoom());
                        c.getSender().setConnectedStatus(ConnectedStatus.ONLINE);
                        conversationUserService.saveUser(c.getSender());
                    }

                    if (c.getReceiver().getConnectedChatRoom() != null &&
                            c.getReceiver().getConnectedChatRoom().getId().equals(
                                    Objects.requireNonNull(c.getSender().getConnectedChatRoom()).getId())) {
//                        log.error("Sending to receiver " + c.getReceiver().getEmail());
//                        messagingTemplate.convertAndSendToUser(chatMessagePayload.getReceiverEmail(), "/queue/messages", cmr);
                        customConvertAndSendToUser.sendToUser(chatMessagePayload.getReceiverEmail(), "/topic/messages", cmr);
                    } else {
//                        log.error("Sending to sender" + c.getSender().getEmail());
//                        messagingTemplate.convertAndSendToUser(chatMessagePayload.getSenderEmail(), "/queue/messages", cmr);
                        customConvertAndSendToUser.sendToUser(chatMessagePayload.getSenderEmail(), "/topic/messages", cmr);
                    }

                    return cmr;
                });

    }

    @Override
//    @Transactional
    public PageableResponse<List<ChatMessageResponse>> getMessages(Long chatRoomId, int offset, int limit) {

        return pageableUtilsCustom.createPageableResponse(
                chatMessageRepository.findAllByChatRoomId(chatRoomId, new ChunkRequest(offset, limit, Sort.by(Sort.Direction.DESC, "timestamp"))),
                chatMessageMapper::fromModelToResponse
        );
    }


}
