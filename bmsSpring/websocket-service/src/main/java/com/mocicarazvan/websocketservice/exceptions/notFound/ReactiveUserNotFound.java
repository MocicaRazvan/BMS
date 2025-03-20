package com.mocicarazvan.websocketservice.exceptions.notFound;

import lombok.Getter;

@Getter
public class ReactiveUserNotFound extends NotFoundBase {
    public String email;

    public ReactiveUserNotFound(String email) {
        super("User not found with email: " + email);
        this.email = email;
    }
}
