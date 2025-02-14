package com.mocicarazvan.gatewayservice.controllers;


import com.mocicarazvan.gatewayservice.dtos.csrf.CsrfValidationBody;
import com.mocicarazvan.gatewayservice.dtos.csrf.CsrfValidationResponse;
import com.mocicarazvan.gatewayservice.services.NextCsrfValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/csrf")
public class CsrfController {

    private final NextCsrfValidator nextCsrfValidator;

    @PostMapping("/validate")
    public Mono<ResponseEntity<CsrfValidationResponse>> validateCsrf(
            @Valid @RequestBody CsrfValidationBody csrfValidationBody,
            ServerWebExchange exchange
    ) {
        return nextCsrfValidator.validateCsrf(csrfValidationBody.getCsrf(), exchange.getRequest().getURI().getPath())
                .map(v -> ResponseEntity.ok(
                        CsrfValidationResponse.builder()
                                .valid(v)
                                .build()
                ))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest().body(CsrfValidationResponse.invalid())
                ));

    }
}
