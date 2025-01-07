package com.mocicarazvan.gatewayservice.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ErrorHandler {
    private final ObjectMapper objectMapper;

    public Mono<Void> handleError(String message, ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", message);
        resp.put("timestamp", Instant.now().toString());
        resp.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        resp.put("status", HttpStatus.UNAUTHORIZED.value());
        resp.put("path", exchange.getRequest().getPath().value());
        try {
            return response.writeWith(Mono.just(response.bufferFactory().wrap(objectMapper.writeValueAsBytes(resp))));
        } catch (Exception e) {
            log.error("Error while writing response", e);
            return Mono.error(e);
        }
    }
}
