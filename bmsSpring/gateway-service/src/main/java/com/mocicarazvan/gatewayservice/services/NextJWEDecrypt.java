package com.mocicarazvan.gatewayservice.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.gatewayservice.dtos.jwe.NextJSJWE;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class NextJWEDecrypt {


    private final ObjectMapper objectMapper;
    private final DirectDecrypter directDecrypter;
    private final ThreadPoolTaskScheduler taskScheduler;
    public static final String NULL_COOKIE = "NULL_COOKIE";

    public NextJWEDecrypt(ObjectMapper objectMapper, DirectDecrypter directDecrypter,
                          @Qualifier("threadPoolTaskScheduler") ThreadPoolTaskScheduler taskScheduler) {
        this.objectMapper = objectMapper;
        this.directDecrypter = directDecrypter;
        this.taskScheduler = taskScheduler;
    }


    public Mono<String> getTokenPayload(String token) {
        if (token == null || token.isBlank()) {
            return Mono.just(NULL_COOKIE);
        }

        return Mono.fromCallable(() -> {
                    JWEObject jweObject = JWEObject.parse(token);
                    jweObject.decrypt(directDecrypter);
                    // jwe puts the payload in bytes so its more efficient
                    return objectMapper.readValue(jweObject.getPayload().toBytes(), NextJSJWE.class)
                            .getUser().getToken();
                })
                .subscribeOn(Schedulers.fromExecutor(taskScheduler))
//                .doOnSuccess(_ -> log.info("Decrypted JWE token successfully"))
                .onErrorResume(e -> {
                            log.error("Error decrypting JWE token: {}", e.getMessage());
                            return Mono.just(NULL_COOKIE);
                        }
                );
    }


}
