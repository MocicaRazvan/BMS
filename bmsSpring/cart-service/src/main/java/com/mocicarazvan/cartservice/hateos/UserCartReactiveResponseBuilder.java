package com.mocicarazvan.cartservice.hateos;

import com.mocicarazvan.cartservice.controllers.UserCartController;
import com.mocicarazvan.cartservice.dtos.UserCartResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import org.springframework.stereotype.Component;

@Component
public class UserCartReactiveResponseBuilder extends ReactiveResponseBuilder<UserCartResponse, UserCartController> {
    public UserCartReactiveResponseBuilder() {
        super(new UserCartLinkBuilder());
    }
}