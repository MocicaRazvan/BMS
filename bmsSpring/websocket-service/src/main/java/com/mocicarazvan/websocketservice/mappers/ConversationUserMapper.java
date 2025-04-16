package com.mocicarazvan.websocketservice.mappers;


import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserPayload;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserResponse;
import com.mocicarazvan.websocketservice.mappers.generic.ModelResponseMapper;
import com.mocicarazvan.websocketservice.models.ChatRoom;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.repositories.ChatRoomRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ConversationUserMapper implements ModelResponseMapper<ConversationUser, ConversationUserResponse> {

    @Autowired
    private ChatRoomRepository chatRoomRepository;


    public abstract ConversationUser fromPayloadToModel(ConversationUserPayload conversationUserPayload);

    @Mapping(target = "connectedChatRoom", ignore = true)
    public abstract ConversationUserResponse _fromModelToResponse(ConversationUser conversationUser);

    public ConversationUser copyFromPayload(ConversationUserPayload conversationUserPayload, ConversationUser conversationUser) {
        ChatRoom chatRoom = conversationUserPayload.getConnectedChatRoomId() == null ? null :
                chatRoomRepository.findById(conversationUserPayload.getConnectedChatRoomId()).orElse(null);
        conversationUser.setConnectedChatRoom(chatRoom);
        conversationUser.setConnectedStatus(conversationUserPayload.getConnectedStatus());
        return conversationUser;
    }

    public ConversationUserResponse fromModelToResponse(ConversationUser conversationUser) {
        return conversationUser.map(this::_fromModelToResponse)
                .map(cur -> getConnectedRoomUserResponse(conversationUser, cur));
    }

    private ConversationUserResponse getConnectedRoomUserResponse(ConversationUser conversationUser, ConversationUserResponse cur) {
        cur.setConnectedChatRoom(
                conversationUser.getConnectedChatRoom() == null ? null :
                        ChatRoomResponse.builder()
                                .id(conversationUser.getConnectedChatRoom().getId())
                                .users(
                                        conversationUser.getConnectedChatRoom().getUsers()
                                                .stream()
                                                .map(this::_fromModelToResponse)
                                                .collect(Collectors.toSet())
                                )
                                .build()
        );
        return cur;
    }
}
