package com.mocicarazvan.archiveservice.config;


import com.mocicarazvan.archiveservice.websocket.BatchHandler;
import com.mocicarazvan.archiveservice.websocket.ContainerActionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

@Configuration
public class WebSocketConfig {
    @Bean
    public SimpleUrlHandlerMapping webSocketMapping(BatchHandler batchHandler, ContainerActionHandler containerHandler) {
        return new SimpleUrlHandlerMapping(Map.of(
                "/archive/queue/batch/update", batchHandler,
                "/archive/container/actions", containerHandler)
                , -1);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
