package com.mocicarazvan.fileservice.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.fileservice.dtos.ProgressUpdateDto;
import com.mocicarazvan.fileservice.enums.FileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressWebSocketHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private WebSocketSession session;

    @Mock
    private ReactiveSubscription.Message<String, String> redisMessage;

    @Mock
    private HandshakeInfo handshakeInfo;

    @InjectMocks
    private ProgressWebSocketHandler progressWebSocketHandler;


    @Test
    void handleClosesSessionWhenClientIdIsMissing() {
        when(session.getHandshakeInfo()).thenReturn(handshakeInfo);
        when(handshakeInfo.getUri()).thenReturn(URI.create("/ws/progress/"));
        when(session.close()).thenReturn(Mono.empty());


        StepVerifier.create(progressWebSocketHandler.handle(session))
                .verifyComplete();

        verify(session).close();
    }

    @Test
    void handleClosesSessionWhenFileTypeIsMissing() {
        when(session.getHandshakeInfo()).thenReturn(handshakeInfo);
        when(handshakeInfo.getUri()).thenReturn(URI.create("/ws/progress/client123"));
        when(session.close()).thenReturn(Mono.empty());

        StepVerifier.create(progressWebSocketHandler.handle(session))
                .verifyComplete();

        verify(session).close();
    }

    @Test
    void handleSendsMessagesFromRedisChannel() {
        String clientId = "client123";
        String fileType = "IMAGE";
        String key = clientId + "-" + fileType;

        when(session.getHandshakeInfo()).thenReturn(handshakeInfo);
        when(handshakeInfo.getUri()).thenReturn(URI.create("/ws/progress/" + clientId + "?fileType=" + fileType));
        when(redisTemplate.listenToChannel(key)).thenReturn((Flux) Flux.just(redisMessage));
        when(session.send(any())).thenReturn(Mono.empty());

        StepVerifier.create(progressWebSocketHandler.handle(session))
                .verifyComplete();

        verify(redisTemplate).listenToChannel(key);
        verify(session, atLeastOnce()).send(any());
    }

    @Test
    void sendProgressUpdatePublishesMessageToRedis() throws JsonProcessingException {
        String clientId = "client123";
        FileType fileType = FileType.IMAGE;
        Long index = 1L;
        String key = clientId + "-" + fileType;
        ProgressUpdateDto dto = ProgressUpdateDto.builder()
                .index(index)
                .message("File " + index + " uploaded successfully")
                .fileType(fileType)
                .build();
        String message = "Serialized Message";

        when(objectMapper.writeValueAsString(dto)).thenReturn(message);
        when(redisTemplate.convertAndSend(key, message)).thenReturn(Mono.just(1L));

        progressWebSocketHandler.sendProgressUpdate(clientId, fileType, index);

        verify(redisTemplate).convertAndSend(key, message);
    }

    @Test
    void sendCompletionMessagePublishesCompletionToRedis() throws JsonProcessingException {
        String clientId = "client123";
        FileType fileType = FileType.IMAGE;
        String key = clientId + "-" + fileType;
        ProgressUpdateDto dto = ProgressUpdateDto.builder()
                .index(-1L)
                .message("All files uploaded successfully")
                .fileType(fileType)
                .build();
        String message = "Serialized Completion Message";

        when(objectMapper.writeValueAsString(dto)).thenReturn(message);
        when(redisTemplate.convertAndSend(key, message)).thenReturn(Mono.just(1L));

        progressWebSocketHandler.sendCompletionMessage(clientId, fileType);

        verify(redisTemplate).convertAndSend(key, message);
    }

}