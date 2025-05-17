package com.mocicarazvan.archiveservice.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.archiveservice.utils.WebsocketUtils;
import com.mocicarazvan.archiveservice.validators.QueueNameValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
public class BatchHandler implements WebSocketHandler {

    private final QueueNameValidator queueNameValidator;
    private final ObjectMapper objectMapper;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final String CHANNEL_PREFIX = "archive:queue:batch:update";

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        Mono<Void> sendPing = WebsocketUtils.getPingMessage(session);

        Flux<WebSocketMessage> outboundMessages = redisTemplate.listenToChannel(CHANNEL_PREFIX)
                .map(ReactiveSubscription.Message::getMessage)
                .map(session::textMessage);

        Mono<Void> out = session.send(outboundMessages);

        return Mono.when(out, sendPing);

    }

    public static String getChannelName() {
        return CHANNEL_PREFIX;
    }


}

