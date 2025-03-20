package com.mocicarazvan.websocketservice.service;

import com.mocicarazvan.websocketservice.dtos.PageableBody;
import com.mocicarazvan.websocketservice.dtos.PageableResponse;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomPayload;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponseJoined;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomUserDto;
import com.mocicarazvan.websocketservice.models.ChatRoom;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public interface ChatRoomService {

    ChatRoomResponse createChatRoom(ChatRoomPayload chatRoomPayload);


    List<ChatRoom> getRoomsByUsers(Set<String> emails);


    List<ChatRoomResponse> getChatRooms(String email);

    PageableResponse<List<ChatRoomResponse>> getChatRoomsFiltered(String email, String filterEmail, PageableBody pageableBody);

    void deleteChatRoom(Long id, String senderEmail);

    ChatRoomResponse findAllByEmails(List<String> emails);

    PageableResponse<List<ChatRoomResponseJoined>> getChatRoomsFilteredJoined(String email, String filterReceiver, PageableBody pageableBody);

    ChatRoomResponseJoined findAllByEmailsJoined(List<String> emails);

    void notifyOtherUsersRoomChange(String senderEmail, Function<ChatRoomUserDto, ChatRoomResponse> createRoom);
}
