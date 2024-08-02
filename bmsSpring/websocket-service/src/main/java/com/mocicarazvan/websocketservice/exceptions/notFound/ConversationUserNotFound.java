package com.mocicarazvan.websocketservice.exceptions.notFound;


public class ConversationUserNotFound extends NotFoundBase {
    public String email;

    public ConversationUserNotFound(String email) {
        super("User with email " + email + " not found");
        this.email = email;
    }
}
