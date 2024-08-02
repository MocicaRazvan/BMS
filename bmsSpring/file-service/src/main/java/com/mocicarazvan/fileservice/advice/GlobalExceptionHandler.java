package com.mocicarazvan.fileservice.advice;

import com.mocicarazvan.fileservice.dtos.FileUploadErrorResponse;
import com.mocicarazvan.fileservice.exceptions.FileNotFound;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUnsupportedMediaTypeStatusException(
            UnsupportedMediaTypeStatusException ex, ServerWebExchange exchange) {

        HttpHeaders headers = exchange.getRequest().getHeaders();
        String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Unsupported Media Type");
        response.put("message", ex.getMessage());
        response.put("contentType", contentType);
        response.put("path", exchange.getRequest().getPath().toString());
        response.put("timestamp", System.currentTimeMillis());

        return Mono.just(ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response));
    }

    @ExceptionHandler(FileNotFound.class)
    public Mono<ResponseEntity<FileUploadErrorResponse>> handleFileNotFound(FileNotFound ex, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.badRequest().body(
                FileUploadErrorResponse.builder()
                        .error("File not found")
                        .message(ex.getMessage())
                        .path(exchange.getRequest().getPath().value())
                        .status(HttpStatus.NOT_FOUND.value())
                        .timestamp(Instant.now().toString())
                        .gridFsId(ex.getGridFsId())
                        .build()
        ));
    }
}
