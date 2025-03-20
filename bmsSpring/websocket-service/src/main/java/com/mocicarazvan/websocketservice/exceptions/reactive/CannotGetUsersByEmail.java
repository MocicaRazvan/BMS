package com.mocicarazvan.websocketservice.exceptions.reactive;

import lombok.Getter;

import java.util.Collection;

@Getter
public class CannotGetUsersByEmail extends RuntimeException {
    private final Collection<String> emails;

    public CannotGetUsersByEmail(Collection<String> emails, Throwable cause) {
        super("Cannot get users by emails: " + emails, cause);
        this.emails = emails;
    }
}
