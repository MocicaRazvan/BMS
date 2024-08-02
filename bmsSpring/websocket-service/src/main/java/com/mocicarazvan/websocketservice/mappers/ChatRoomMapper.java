package com.mocicarazvan.websocketservice.mappers;


import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomPayload;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.mappers.generic.ModelResponseMapper;
import com.mocicarazvan.websocketservice.models.ChatRoom;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ChatRoomMapper implements ModelResponseMapper<ChatRoom, ChatRoomResponse> {

    @Autowired
    private ConversationUserService conversationUserService;
    @Autowired
    private ConversationUserMapper conversationUserMapper;

    public ChatRoomResponse fromModelToResponse(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .users(chatRoom.getUsers()
                        .stream()
                        .map(u -> conversationUserMapper.fromModelToResponse(u))
                        .collect(Collectors.toSet()))
                .build();
    }

    public abstract ChatRoom fromDtoToModel(ChatRoomResponse chatRoomResponse);

    public ChatRoom fromPayloadToModel(ChatRoomPayload chatRoomPayload) {
        return ChatRoom.builder().users(chatRoomPayload.getUsers()
                .stream().
                map(u -> conversationUserService.getUserByEmail(u.getEmail()))
                .collect(Collectors.toList())).build();

    }
}
