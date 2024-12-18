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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatRoomService chatRoomService;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final ConversationUserService conversationUserService;
    private final PageableUtilsCustom pageableUtilsCustom;
    private final CustomConvertAndSendToUser customConvertAndSendToUser;


    @Override
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
                    ChatMessageResponse cmr = chatMessageMapper.fromModelToResponse(c);
                    // todo make the check bette
                    // extra safe check just to be sure

                    if (c.getSender().getConnectedChatRoom() == null && c.getChatRoom() != null) {
                        c.getSender().setConnectedChatRoom(c.getChatRoom());
                        c.getSender().setConnectedStatus(ConnectedStatus.ONLINE);
                        conversationUserService.saveUser(c.getSender());
                    }

                    if (c.getReceiver().getConnectedChatRoom() != null &&
                            c.getReceiver().getConnectedChatRoom().getId().equals(
                                    Objects.requireNonNull(c.getSender().getConnectedChatRoom()).getId())) {

                        customConvertAndSendToUser.sendToUser(chatMessagePayload.getReceiverEmail(), "/topic/messages", cmr);
                    } else {

                        customConvertAndSendToUser.sendToUser(chatMessagePayload.getSenderEmail(), "/topic/messages", cmr);
                    }

                    return cmr;
                });

    }

    @Override
    public PageableResponse<List<ChatMessageResponse>> getMessages(Long chatRoomId, int offset, int limit) {

        return pageableUtilsCustom.createPageableResponse(
                chatMessageRepository.findAllByChatRoomId(chatRoomId, new ChunkRequest(offset, limit, Sort.by(Sort.Direction.DESC, "timestamp"))),
                chatMessageMapper::fromModelToResponse
        );
    }


}
