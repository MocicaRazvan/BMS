package com.mocicarazvan.websocketservice.advice;


import com.mocicarazvan.websocketservice.dtos.errors.BaseErrorResponse;
import com.mocicarazvan.websocketservice.exceptions.MoreThenOneChatRoom;
import com.mocicarazvan.websocketservice.exceptions.SameUserChatRoom;
import com.mocicarazvan.websocketservice.exceptions.UserIsConnectedToTheRoom;
import com.mocicarazvan.websocketservice.exceptions.notFound.NotFoundBase;
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

@ControllerAdvice
@RequiredArgsConstructor
public class Advice extends BaseAdvice {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CustomConvertAndSendToUser customConvertAndSendToUser;

    @ExceptionHandler({NotFoundBase.class, MoreThenOneChatRoom.class, SameUserChatRoom.class, UserIsConnectedToTheRoom.class})
    public ResponseEntity<BaseErrorResponse> handleBadRequest(RuntimeException e, HttpServletRequest request) {

        return handleWithMessage(HttpStatus.BAD_REQUEST, e, request);
    }

    @MessageExceptionHandler({NotFoundBase.class, MoreThenOneChatRoom.class, SameUserChatRoom.class})
    public void handleBadRequest(RuntimeException e, Message<?> message,
                                 StompHeaderAccessor accessor) {

//        String username = Objects.requireNonNull(accessor.getUser()).getName();
//
//
//        WebSocketErrorResponse resp = WebSocketErrorResponse.builder()
//                .message(e.getMessage())
//                .timestamp(Instant.now().toString())
//                .error(e.getClass().getSimpleName())
//                .sessionId(accessor.getSessionId())
//                .destination(accessor.getDestination())
//                .status(HttpStatus.BAD_REQUEST.value())
//                .payloadType(message.getPayload().getClass().getSimpleName())
//                .build();
//
//
//        if (username != null) {
//            simpMessagingTemplate.convertAndSendToUser(username, "/queue/errors", resp);
//        } else {
//            simpMessagingTemplate.convertAndSend("/queue/errors", resp);
//        }
        handleWithMessageWs(e, message, accessor, HttpStatus.BAD_REQUEST, simpMessagingTemplate, customConvertAndSendToUser);

    }
}
