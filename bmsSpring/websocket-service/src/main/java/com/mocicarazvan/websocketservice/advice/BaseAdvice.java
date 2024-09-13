package com.mocicarazvan.websocketservice.advice;

import com.mocicarazvan.websocketservice.dtos.errors.BaseErrorResponse;
import com.mocicarazvan.websocketservice.dtos.errors.WebSocketErrorResponse;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.time.Instant;
import java.util.Objects;

public abstract class BaseAdvice {
    protected BaseErrorResponse respWithMessage(HttpStatus status, Exception error, HttpServletRequest request) {
        return BaseErrorResponse.builder()
                .message(error.getMessage())
                .timestamp(Instant.now().toString())
                .error(status.getReasonPhrase())
                .status(status.value())
                .path(request.getRequestURI())
                .build();
    }


    protected WebSocketErrorResponse respWithMessageWs(RuntimeException e, StompHeaderAccessor accessor, HttpStatus status, Message<?> message) {
        return WebSocketErrorResponse.builder()
                .message(e.getMessage())
                .timestamp(Instant.now().toString())
                .error(e.getClass().getSimpleName())
                .sessionId(accessor.getSessionId())
                .destination(accessor.getDestination())
                .status(status.value())
                .payloadType(message.getPayload().getClass().getSimpleName())
                .build();
    }

    protected ResponseEntity<BaseErrorResponse> handleWithMessage(HttpStatus status, Exception exception, HttpServletRequest request) {
        return ResponseEntity.status(status).body(respWithMessage(status, exception, request));
    }

    protected void handleWithMessageWs(RuntimeException e, Message<?> message, StompHeaderAccessor accessor, HttpStatus status,
                                       SimpMessagingTemplate simpMessagingTemplate,
                                       CustomConvertAndSendToUser customConvertAndSendToUser
    ) {
        WebSocketErrorResponse resp = respWithMessageWs(e, accessor, status, message);
        String username = Objects.requireNonNull(accessor.getUser()).getName();
        if (username != null) {
//            simpMessagingTemplate.convertAndSendToUser(username, "/queue/errors", resp);
            customConvertAndSendToUser.sendToUser(username, "/queue/errors", resp);
        } else {
            simpMessagingTemplate.convertAndSend("/queue/errors", resp);
        }
    }
}

