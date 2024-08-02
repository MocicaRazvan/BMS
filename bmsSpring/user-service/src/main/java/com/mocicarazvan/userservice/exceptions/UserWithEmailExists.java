package com.mocicarazvan.userservice.exceptions;

import lombok.Getter;

@Getter
public class UserWithEmailExists extends RuntimeException {
    public UserWithEmailExists(String email) {
        super("User with email " + email + " already exists");
    }
}
