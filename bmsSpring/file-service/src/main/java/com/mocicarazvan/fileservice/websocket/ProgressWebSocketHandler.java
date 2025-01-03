package com.mocicarazvan.fileservice.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.fileservice.dtos.ProgressUpdateDto;
import com.mocicarazvan.fileservice.enums.FileType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
@Slf4j

public class ProgressWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public ProgressWebSocketHandler(ObjectMapper objectMapper, ReactiveRedisTemplate<String, String> redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // Extract clientId from the path
        String clientId = session.getHandshakeInfo().getUri().getPath().split("/ws/progress/")[1];

        String stringFileType = UriComponentsBuilder.fromUri(session.getHandshakeInfo().getUri())
                .build()
                .getQueryParams()
                .getFirst("fileType");

        if (clientId == null || clientId.isEmpty() || stringFileType == null || stringFileType.isEmpty()) {
            return session.close();
        }

        FileType fileType = FileType.valueOf(stringFileType);
        String key = clientId + "-" + fileType;

        return redisTemplate.listenToChannel(key)
                .map(ReactiveSubscription.Message::getMessage)
                .flatMap(message -> session.send(Mono.just(session.textMessage(message))))
                .takeUntilOther(session.receive()).then();
    }

    public void sendProgressUpdate(String clientId, FileType fileType, Long index) {
        String key = clientId + "-" + fileType.toString();

        try {
            String message = objectMapper.writeValueAsString(
                    ProgressUpdateDto.builder()
                            .index(index)
                            .message("File " + index + " uploaded successfully")
                            .fileType(fileType)
                            .build()
            );
            redisTemplate.convertAndSend(key, message)
                    .subscribe();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    public void sendCompletionMessage(String clientId, FileType fileType) {
        String key = clientId + "-" + fileType.toString();
        try {
            String message = objectMapper.writeValueAsString(
                    ProgressUpdateDto.builder()
                            .index(-1L)
                            .message("All files uploaded successfully")
                            .fileType(fileType)
                            .build()
            );
            redisTemplate.convertAndSend(key, message).subscribe();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }


}