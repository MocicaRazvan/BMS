package com.mocicarazvan.fileservice.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.fileservice.dtos.ProgressUpdateDto;
import com.mocicarazvan.fileservice.enums.FileType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProgressWebSocketHandler implements WebSocketHandler {

    private final Map<String, WebSocketSession> clientSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ProgressWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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

        clientSessions.put(key, session);

        return session.receive()
                .doFinally(signalType -> clientSessions.remove(key))
                .then();  // Keep the session open
    }

    public void sendProgressUpdate(String clientId, FileType fileType, Long index) {
        String key = clientId + "-" + fileType.toString();
        WebSocketSession session = clientSessions.get(key);
        if (session != null && session.isOpen()) {
            try {

                session.send(Mono.just(session.textMessage(objectMapper.writeValueAsString(
                                ProgressUpdateDto.builder()
                                        .index(index)
                                        .message("File " + index + " uploaded successfully")
                                        .fileType(fileType)
                                        .build()
                        ))))
                        .subscribe();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendCompletionMessage(String clientId, FileType fileType) {
        String key = clientId + "-" + fileType.toString();
        WebSocketSession session = clientSessions.get(key);
        if (session != null && session.isOpen()) {

            try {
                session.send(Mono.just(session.textMessage(objectMapper.writeValueAsString(
                                ProgressUpdateDto.builder()
                                        .index(-1L)
                                        .message("All files uploaded successfully")
                                        .fileType(fileType)
                                        .build()
                        ))))
                        .subscribe();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}