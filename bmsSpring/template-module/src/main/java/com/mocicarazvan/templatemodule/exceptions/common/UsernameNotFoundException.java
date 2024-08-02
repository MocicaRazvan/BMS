package com.mocicarazvan.templatemodule.exceptions.common;


import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundBase;

public class UsernameNotFoundException extends NotFoundBase {
    public UsernameNotFoundException(String message) {
        super(message);
    }
}
