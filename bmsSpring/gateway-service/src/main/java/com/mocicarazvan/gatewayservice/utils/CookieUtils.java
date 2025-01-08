package com.mocicarazvan.gatewayservice.utils;


import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;

@Component
public class CookieUtils {
    public String getCookie(ServerWebExchange exchange, String[] cookieNames) {
        for (String cookie : cookieNames) {
            if (exchange.getRequest().getCookies().getFirst(cookie) != null) {
                return Objects.requireNonNull(exchange.getRequest().getCookies().getFirst(cookie)).getValue();
            }
        }
        return null;
    }

}
