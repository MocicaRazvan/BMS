package com.mocicarazvan.archiveservice.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.archiveservice.dtos.websocket.NotifyContainerAction;
import com.mocicarazvan.archiveservice.services.ContainerActionPublisher;
import com.mocicarazvan.archiveservice.services.UserContainerActionService;
import com.mocicarazvan.archiveservice.utils.UserHeaderUtils;
import com.mocicarazvan.archiveservice.utils.WebsocketUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@Slf4j
@Component
@Getter
public class ContainerActionHandler implements WebSocketHandler, ContainerActionPublisher {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserContainerActionService userContainerActionService;
    public static final String CHANNEL_PREFIX = "archive:notifications";
    private final Duration expireDuration;

    public ContainerActionHandler(ReactiveRedisTemplate<String, Object> redisTemplate,
                                  @Qualifier("redisObjectMapper") ObjectMapper objectMapper, UserContainerActionService userContainerActionService,
                                  @Value("${spring.custom.cache.notificationsExpire:5184000}") int expireAfterWrite) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.userContainerActionService = userContainerActionService;
        this.expireDuration = Duration.ofSeconds(expireAfterWrite);
    }


    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Mono<Void> sendPing = WebsocketUtils.getPingMessage(session);

        String userId = UserHeaderUtils.getFromWebSocketSession(session);

        if (userId == null || userId.isEmpty()) {
            return session.close();
        }


        Flux<WebSocketMessage> outboundMessages = redisTemplate.listenToChannel(CHANNEL_PREFIX)
                .map(ReactiveSubscription.Message::getMessage)
                .cast(NotifyContainerAction.class)
                .flatMap(action -> userContainerActionService.saveFromActionAndUserId(action, userId)
                        .thenReturn(session.textMessage(Objects.requireNonNull(convertMessageToJson(action))))
                );
        Mono<Void> out = session.send(outboundMessages);

        Mono<Void> in = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .filter(msg -> msg != null && !msg.trim().isEmpty())
                .flatMap(payload -> handleActionDelete(userId, payload)).then();

        return Mono.when(out, in, sendPing);
    }


    private String getIndexKey(String userId) {
        return CHANNEL_PREFIX + ":" + userId + ":index";
    }

    private String getDataKey(String userId) {
        return CHANNEL_PREFIX + ":" + userId + ":data";
    }

    private Mono<Void> handleActionDelete(String userId, String payload) {
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.path("type").asText();

            if (!"delete".equalsIgnoreCase(type)) {
                return Mono.empty();
            }

            JsonNode idsNode = jsonNode.path("ids");
            if (!idsNode.isArray() || idsNode.isEmpty()) {
                return Mono.empty();
            }

            return Flux.fromIterable(idsNode)
                    .map(JsonNode::asText)
                    .collectList()
                    .flatMap(ids -> userContainerActionService.deleteAllByUserIdAndActionIdIn(
                            userId,
                            ids
                    ));

        } catch (Exception e) {
            log.warn("Ignoring non-deletion message: {}", payload);
            return Mono.empty();
        }
    }

    private String convertMessageToJson(NotifyContainerAction action) {
        try {
            return objectMapper.writeValueAsString(action);
        } catch (Exception e) {
            log.error("Error converting message to JSON: {}", e.getMessage());
            return null;
        }
    }


    public Mono<Long> sendContainerActionMessage(NotifyContainerAction action) {
        return redisTemplate.convertAndSend(CHANNEL_PREFIX, action);
    }
}
