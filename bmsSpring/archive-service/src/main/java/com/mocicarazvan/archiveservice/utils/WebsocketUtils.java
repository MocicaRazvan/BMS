package com.mocicarazvan.archiveservice.utils;

import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class WebsocketUtils {
    public static Mono<Void> getPingMessage(WebSocketSession session) {
        return session.send(
                        Flux.interval(Duration.ofSeconds(5), Duration.ofSeconds(5))
                                .map(_ -> session.pingMessage(_ -> session.bufferFactory().allocateBuffer(1))))
                .onErrorResume(_ ->
                        session.close()
                );
    }
}
