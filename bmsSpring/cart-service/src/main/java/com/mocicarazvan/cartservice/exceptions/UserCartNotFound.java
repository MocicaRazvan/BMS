package com.mocicarazvan.cartservice.exceptions;

import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundBase;

public class UserCartNotFound extends NotFoundBase {
    private Long userId;

    public UserCartNotFound(Long userId) {
        super("User cart not found for user with id: " + userId);
    }
}
