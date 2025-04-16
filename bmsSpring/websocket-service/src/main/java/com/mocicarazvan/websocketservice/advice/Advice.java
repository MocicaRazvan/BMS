package com.mocicarazvan.websocketservice.advice;


import com.mocicarazvan.websocketservice.dtos.errors.BaseErrorResponse;
import com.mocicarazvan.websocketservice.exceptions.MoreThenOneChatRoom;
import com.mocicarazvan.websocketservice.exceptions.SameUserChatRoom;
import com.mocicarazvan.websocketservice.exceptions.UserIsConnectedToTheRoom;
import com.mocicarazvan.websocketservice.exceptions.notFound.NotFoundBase;
import com.mocicarazvan.websocketservice.exceptions.reactive.CannotGetUsersByEmail;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.concurrent.CompletionException;

@ControllerAdvice
@RequiredArgsConstructor
public class Advice extends BaseAdvice {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CustomConvertAndSendToUser customConvertAndSendToUser;

    @ExceptionHandler({NotFoundBase.class, MoreThenOneChatRoom.class, SameUserChatRoom.class, UserIsConnectedToTheRoom.class, CannotGetUsersByEmail.class})
    public ResponseEntity<BaseErrorResponse> handleBadRequest(RuntimeException e, HttpServletRequest request) {

        return handleWithMessage(HttpStatus.BAD_REQUEST, e, request);
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<BaseErrorResponse> handleCompletionException(CompletionException e, HttpServletRequest request) {
        Throwable cause = e.getCause();

        if (cause instanceof NotFoundBase ||
                cause instanceof MoreThenOneChatRoom ||
                cause instanceof SameUserChatRoom ||
                cause instanceof UserIsConnectedToTheRoom
                || cause instanceof CannotGetUsersByEmail
        ) {
            return handleWithMessage(HttpStatus.BAD_REQUEST, (RuntimeException) cause, request);
        }
        return handleWithMessage(HttpStatus.INTERNAL_SERVER_ERROR, new RuntimeException("Unexpected async error"), request);
    }


    @MessageExceptionHandler({NotFoundBase.class, MoreThenOneChatRoom.class, SameUserChatRoom.class})
    public void handleBadRequest(RuntimeException e, Message<?> message,
                                 StompHeaderAccessor accessor) {
        handleWithMessageWs(e, message, accessor, HttpStatus.BAD_REQUEST, simpMessagingTemplate, customConvertAndSendToUser);

    }
}
