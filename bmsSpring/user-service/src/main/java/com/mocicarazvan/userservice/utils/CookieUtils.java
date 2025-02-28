package com.mocicarazvan.userservice.utils;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtils {

    private static final String COOKIE_NAME = "authToken";

    public static ResponseCookie createCookie(String token) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .maxAge(Duration.ofDays(30))
                .path("/")
                .build();
    }
}
