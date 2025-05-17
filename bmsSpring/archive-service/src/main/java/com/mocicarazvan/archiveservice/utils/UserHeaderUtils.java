package com.mocicarazvan.archiveservice.utils;

import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.server.ServerWebExchange;

public class UserHeaderUtils {
    public static final String AUTH_USER_ID = "x-auth-user-id";

    public static String getFromWebSocketSession(WebSocketSession session) {
        return session.getHandshakeInfo().getHeaders().getFirst(AUTH_USER_ID);
    }

    public static String getFromServerWebExchange(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(AUTH_USER_ID);
    }
}
