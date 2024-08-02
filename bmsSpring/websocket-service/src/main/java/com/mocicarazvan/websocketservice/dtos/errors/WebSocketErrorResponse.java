package com.mocicarazvan.websocketservice.dtos.errors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WebSocketErrorResponse {
    private String message;
    private String timestamp;
    private String error;
    private String sessionId;
    private String destination;
    private int status;
    private String payloadType;
}