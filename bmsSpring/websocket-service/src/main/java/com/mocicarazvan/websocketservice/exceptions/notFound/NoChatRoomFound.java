package com.mocicarazvan.websocketservice.exceptions.notFound;

import java.util.Set;

public class NoChatRoomFound extends NotFoundBase {
    public Set<String> users;
    public Long id;


    public NoChatRoomFound(Set<String> users) {
        super("No chat room found for users: " + users);
        this.users = users;
    }

    public NoChatRoomFound(Long id) {
        super("No chat room found for id: " + id);
        this.id = id;
    }
}