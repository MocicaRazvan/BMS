package com.mocicarazvan.gatewayservice.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.gatewayservice.dtos.jwe.NextJSJWE;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class NextJWEDecrypt {


    private final ObjectMapper objectMapper;
    private final DirectDecrypter directDecrypter;
    public static final String NULL_COOKIE = "NULL_COOKIE";


    public Mono<String> getTokenPayload(String token) {
        if (token == null || token.isBlank()) {
            return Mono.just(NULL_COOKIE);
        }

        return Mono.fromCallable(() -> {
                    JWEObject jweObject = JWEObject.parse(token);
                    jweObject.decrypt(directDecrypter);
                    return objectMapper.readValue(jweObject.getPayload().toString(), NextJSJWE.class)
                            .getUser().getToken();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(_ -> Mono.just(NULL_COOKIE)
                );
    }


}
