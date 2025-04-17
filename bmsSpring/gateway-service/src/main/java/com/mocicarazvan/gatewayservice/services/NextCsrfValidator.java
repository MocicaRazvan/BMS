package com.mocicarazvan.gatewayservice.services;


import com.mocicarazvan.gatewayservice.config.NextAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

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
    private final ThreadLocal<MessageDigest> sha256ThreadLocal = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("Error initializing SHA-256 MessageDigest", e);
            throw new RuntimeException(e);
        }
    });
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
//        log.info("CSRF: {}", csrf);
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

    private String computeSha256Hash(String data) {
        byte[] hashBytes = sha256ThreadLocal.get().digest(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes);
    }

    public boolean isExemptedPath(String path) {
        return nextAuthProperties.getCsrfExemptedUrls().stream().anyMatch(p -> antPathMatcher.match(p, path));
    }
}
