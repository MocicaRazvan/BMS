package com.mocicarazvan.websocketservice.service;

import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomUserDto;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserPayload;
import com.mocicarazvan.websocketservice.dtos.user.ConversationUserResponse;
import com.mocicarazvan.websocketservice.dtos.user.JoinedConversationUser;
import com.mocicarazvan.websocketservice.enums.ConnectedStatus;
import com.mocicarazvan.websocketservice.models.ConversationUser;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ConversationUserService {

    ConversationUserResponse addUser(ConversationUserPayload conversationUserPayload);

    ConversationUserResponse changeUserConnectedStatus(ConnectedStatus connectedStatus, String email);

    ConversationUser getUserByEmail(String email);

    ConversationUser getOrCreateUserByEmail(String email);

    CompletableFuture<ConversationUser> getUserByEmailAsync(String email);

    ConversationUser saveUser(ConversationUser conversationUser);

    ConversationUser saveUserByEmailIfNotExist(String email);

    List<ConversationUserResponse> getConnectedUsers();

    ConversationUserResponse changeUserChatRoom(ChatRoomUserDto chatRoomUserDto);

    CompletableFuture<List<JoinedConversationUser>> getJoinedConnectedConversationUsers();


    CompletableFuture<List<JoinedConversationUser>> fromConversationToJoinedUsers(Collection<ConversationUserResponse> conversationUserResponses);
}
