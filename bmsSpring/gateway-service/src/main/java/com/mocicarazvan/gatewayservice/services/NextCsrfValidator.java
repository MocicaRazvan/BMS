package com.mocicarazvan.gatewayservice.services;


import com.mocicarazvan.gatewayservice.config.NextAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class NextCsrfValidator {
    private final NextAuthProperties nextAuthProperties;
    private final AntPathMatcher antPathMatcher;
    public static final String[] NEXT_CSRF_COOKIES = {
            "__Host-next-auth.csrf-token"
            , "next-auth.csrf-token"
    };

    public static final String NEXT_CSRF_HEADER_TOKEN = "x-csrf-token";
    public static final String NEXT_CSRF_HEADER = "x-csrf-header";


    public Mono<Boolean> validateCsrf(String csrf, String rawToken, String requestUri) {

        if (!nextAuthProperties.isCsrfEnabled()) {
            return Mono.just(true);
        }
        if (isExemptedPath(requestUri)) {
            return Mono.just(true);
        }

        if (requestUri.contains("/files/download/")) {
            return Mono.just(true);
        } else if (csrf == null || csrf.isBlank()) {
            return Mono.just(false);
        } else if (rawToken == null || rawToken.isBlank()) {
            return Mono.just(false);
        }
        log.info("CSRF: {}", csrf);
        return Mono.fromCallable(() -> {
                    String[] csrfParts = splitCsrf(csrf);
                    String requestToken = csrfParts[0];
                    String requestHash = csrfParts[1];

//                    log.error("RequestToken: {} RawToken: {}", requestToken, rawToken);

                    if (!requestToken.equals(rawToken)) {
                        log.error("CSRF token mismatch requestToken: {} rawToken: {}", requestToken, rawToken);
                        return false;
                    }

                    String validHash = computeSha256Hash(requestToken + nextAuthProperties.getSecret());

                    return requestHash.equals(validHash);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(_ -> Mono.just(false)
                );
    }

    public Mono<Boolean> validateCsrf(String csrf, String requestUri) {
        return validateCsrf(csrf, splitCsrf(csrf)[0], requestUri);
    }

    public String[] splitCsrf(String csrf) {
        String delimiter = csrf.contains("|") ? "\\|" : "%7C";
        String[] parts = csrf.split(delimiter);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid CSRF token");
        }

        return parts;
    }

    private String computeSha256Hash(String data) throws NoSuchAlgorithmException {
        // useless to make thread local bc its gonna run on virtual thread bc its super fast anyway
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes);
    }

    public boolean isExemptedPath(String path) {
        return nextAuthProperties.getCsrfExemptedUrls().stream().anyMatch(p -> antPathMatcher.match(p, path));
    }
}
