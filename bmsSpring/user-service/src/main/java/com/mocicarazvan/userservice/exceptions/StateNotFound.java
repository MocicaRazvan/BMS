package com.mocicarazvan.userservice.exceptions;

import lombok.Getter;

@Getter
public class StateNotFound extends RuntimeException {
    public StateNotFound() {
        super("State not found");
    }
}
