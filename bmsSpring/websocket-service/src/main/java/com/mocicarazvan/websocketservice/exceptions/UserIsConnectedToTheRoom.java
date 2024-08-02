package com.mocicarazvan.websocketservice.exceptions;

import lombok.Getter;

@Getter
public class UserIsConnectedToTheRoom extends RuntimeException {
    private final Long userId;

    public UserIsConnectedToTheRoom(Long userId) {
        super("User with id: " + userId + " is connected");
        this.userId = userId;
    }
}
