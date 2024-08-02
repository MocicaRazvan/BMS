package com.mocicarazvan.websocketservice.mappers;

import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.message.ChatMessagePayload;
import com.mocicarazvan.websocketservice.dtos.message.ChatMessageResponse;
import com.mocicarazvan.websocketservice.mappers.generic.ModelResponseMapper;
import com.mocicarazvan.websocketservice.models.ChatMessage;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ChatMessageMapper implements ModelResponseMapper<ChatMessage, ChatMessageResponse> {

    @Autowired
    private ConversationUserService conversationUserService;
    @Autowired
    private ConversationUserMapper conversationUserMapper;


    public ChatMessage fromPayloadToModel(ChatMessagePayload chatMessagePayload) {
        return ChatMessage.builder()
                .sender(conversationUserService.getUserByEmail(chatMessagePayload.getSenderEmail()))
                .receiver(conversationUserService.getUserByEmail(chatMessagePayload.getReceiverEmail()))
                .content(chatMessagePayload.getContent())
                .build();
    }

    @Mapping(target = "chatRoom", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "receiver", ignore = true)
    public abstract ChatMessageResponse _fromModelToResponse(ChatMessage chatMessage);

    public ChatMessageResponse fromModelToResponse(ChatMessage chatMessage) {
        return chatMessage.map(this::_fromModelToResponse)
                .map(cur -> {
                    cur.setChatRoom(
                            chatMessage.getChatRoom() == null ? null :
                                    ChatRoomResponse.builder()
                                            .id(chatMessage.getChatRoom().getId())
                                            .users(
                                                    chatMessage.getChatRoom().getUsers()
                                                            .stream()
                                                            .map(conversationUserMapper::_fromModelToResponse)
                                                            .collect(java.util.stream.Collectors.toSet())
                                            )
                                            .build()
                    );
                    cur.setSender(conversationUserMapper.fromModelToResponse(chatMessage.getSender()));
                    cur.setReceiver(conversationUserMapper.fromModelToResponse(chatMessage.getReceiver()));
                    return cur;
                });
    }
}
