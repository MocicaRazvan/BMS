package com.mocicarazvan.archiveservice.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.archiveservice.validators.QueueNameValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

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

        return redisTemplate.listenToChannel(CHANNEL_PREFIX)
                .map(ReactiveSubscription.Message::getMessage)
                .flatMap(message -> session.send(Mono.just(session.textMessage(message))))
                .takeUntilOther(session.receive()).then();
    }

    public static String getChannelName() {
        return CHANNEL_PREFIX;
    }


}

