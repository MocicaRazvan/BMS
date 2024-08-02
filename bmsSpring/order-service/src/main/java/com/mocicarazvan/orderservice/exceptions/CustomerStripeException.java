package com.mocicarazvan.orderservice.exceptions;

import lombok.Getter;

@Getter
public class CustomerStripeException extends RuntimeException {
    private final String email;

    public CustomerStripeException(String email) {
        super("Customer with email: " + email + " cant be found or created");
        this.email = email;
    }
}
