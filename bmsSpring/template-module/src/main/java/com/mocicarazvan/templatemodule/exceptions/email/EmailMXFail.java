package com.mocicarazvan.templatemodule.exceptions.email;

import lombok.Getter;

@Getter
public class EmailMXFail extends RuntimeException {
    private final String to;


    public EmailMXFail(String to) {
        super("The email " + to + " does not have a valid MX record");
        this.to = to;
    }
}
