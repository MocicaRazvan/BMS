package com.mocicarazvan.gatewayservice.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.gatewayservice.dtos.NextJSJWE;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
@Component
@RequiredArgsConstructor
public class NextAuthDecrypt {
    @Value("${next.auth.auth.secret}")
    private String secretKey;

    @Value("${next.auth.auth.csrf.enabled:true}")
    private boolean csrfEnabled;

    private final ObjectMapper objectMapper;
    private final DirectDecrypter directDecrypter;
    public static final String NULL_COOKIE = "NULL_COOKIE";


    public Mono<String> getTokenPayload(String token) {
        if (token == null || token.isBlank()) {
            return Mono.just(NULL_COOKIE);
        }

        return Mono.just(true)
                .flatMap(_ -> Mono.fromCallable(() -> {
                            JWEObject jweObject = JWEObject.parse(token);
                            jweObject.decrypt(directDecrypter);
                            return objectMapper.readValue(jweObject.getPayload().toString(), NextJSJWE.class)
                                    .getUser().getToken();
                        })
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(_ -> Mono.just(NULL_COOKIE)
                        ));
    }

    public Mono<Boolean> validateCsrf(String csrf, String requestUri) {

        if (!csrfEnabled) {
            return Mono.just(true);
        }

        if (requestUri.contains("/files/download/")) {
            return Mono.just(true);
        } else if (csrf == null || csrf.isBlank()) {
            return Mono.just(false);
        }
        log.info("CSRF: {}", csrf);
        return Mono.just(true)
                .flatMap(_ -> Mono.fromCallable(() -> {
                                    String delimiter = csrf.contains("|") ? "\\|" : "%7C";
                                    String[] parts = csrf.split(delimiter);
                                    if (parts.length != 2) {
                                        return false;
                                    }

                                    String requestToken = parts[0];
                                    String requestHash = parts[1];

                                    String validHash = computeSha256Hash(requestToken + secretKey);

                                    return requestHash.equals(validHash);
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                                .onErrorResume(_ -> Mono.just(false))
                );
    }

    private String computeSha256Hash(String data) throws NoSuchAlgorithmException {
        // useless to make thread local bc its gonna run on virtual thread bc its super fast anyway
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes);
    }

}
