package com.mocicarazvan.fileservice.config;


import com.mocicarazvan.fileservice.websocket.ProgressWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.netty.http.server.WebsocketServerSpec;

import java.util.Map;

@Configuration
public class WebSocketConfig {
    @Bean
    public SimpleUrlHandlerMapping webSocketMapping(ProgressWebSocketHandler progressWebSocketHandler) {
        return new SimpleUrlHandlerMapping(Map.of("/files/ws/progress/{clientId}", progressWebSocketHandler), -1);
    }

    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService(
                new ReactorNettyRequestUpgradeStrategy(WebsocketServerSpec.builder().compress(true)));
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter(WebSocketService webSocketService) {
        return new WebSocketHandlerAdapter(webSocketService);
    }
}
