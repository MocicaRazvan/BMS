package com.mocicarazvan.templatemodule.exceptions.email;


import lombok.Getter;


@Getter
public class EmailFailToSend extends RuntimeException {
    private final String subject;
    private final String to;
    private final String content;

    public EmailFailToSend(String to, String subject, String content) {
        super("Email fail to send to " + to + " with subject " + subject);
        this.subject = subject;
        this.to = to;
        this.content = content;
    }
}
