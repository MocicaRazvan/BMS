package com.mocicarazvan.gatewayservice.services;

import com.mocicarazvan.gatewayservice.config.NextAuthProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NextCsrfValidatorTest {

    @Mock
    private NextAuthProperties nextAuthProperties;

    @Mock
    private AntPathMatcher antPathMatcher;

    @InjectMocks
    private NextCsrfValidator nextCsrfValidator;

    @Test
    void validateCsrfReturnsTrueWhenCsrfIsDisabled() {
        when(nextAuthProperties.isCsrfEnabled()).thenReturn(false);

        Mono<Boolean> result = nextCsrfValidator.validateCsrf("csrf|hash", "csrf", "/some/path");

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void validateCsrfReturnsTrueForExemptedPath() {
        when(nextAuthProperties.isCsrfEnabled()).thenReturn(true);
        when(nextAuthProperties.getCsrfExemptedUrls()).thenReturn(List.of("/exempted/**"));
        when(antPathMatcher.match("/exempted/**", "/exempted/path")).thenReturn(true);

        Mono<Boolean> result = nextCsrfValidator.validateCsrf("csrf|hash", "csrf", "/exempted/path");

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void validateCsrfReturnsFalseWhenCsrfTokenIsNull() {
        when(nextAuthProperties.isCsrfEnabled()).thenReturn(true);

        Mono<Boolean> result = nextCsrfValidator.validateCsrf(null, "csrf", "/some/path");

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void validateCsrfReturnsFalseWhenRawTokenIsNull() {
        when(nextAuthProperties.isCsrfEnabled()).thenReturn(true);

        Mono<Boolean> result = nextCsrfValidator.validateCsrf("csrf|hash", null, "/some/path");

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void validateCsrfReturnsFalseWhenTokensMismatch() {
        when(nextAuthProperties.isCsrfEnabled()).thenReturn(true);
        when(nextAuthProperties.getCsrfExemptedUrls()).thenReturn(List.of());


        Mono<Boolean> result = nextCsrfValidator.validateCsrf("csrf|hash", "differentCsrf", "/some/path");

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void validateCsrfReturnsTrueForValidCsrfToken() throws NoSuchAlgorithmException {
        when(nextAuthProperties.isCsrfEnabled()).thenReturn(true);
        when(nextAuthProperties.getSecret()).thenReturn("secret");

        String csrf = "csrf";
        String validHash = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest((csrf + "secret").getBytes(StandardCharsets.UTF_8))
        );
        String csrfToken = csrf + "|" + validHash;

        Mono<Boolean> result = nextCsrfValidator.validateCsrf(csrfToken, csrf, "/some/path");

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void splitCsrfThrowsExceptionForInvalidToken() {
        String invalidCsrf = "invalidToken";

        assertThrows(IllegalArgumentException.class, () -> nextCsrfValidator.splitCsrf(invalidCsrf));
    }

    @Test
    void isExemptedPathReturnsTrueForMatchingPath() {
        when(nextAuthProperties.getCsrfExemptedUrls()).thenReturn(List.of("/exempted/**"));
        when(antPathMatcher.match("/exempted/**", "/exempted/path")).thenReturn(true);

        boolean result = nextCsrfValidator.isExemptedPath("/exempted/path");

        assertTrue(result);
    }

    @Test
    void isExemptedPathReturnsFalseForNonMatchingPath() {
        when(nextAuthProperties.getCsrfExemptedUrls()).thenReturn(List.of("/exempted/**"));
        when(antPathMatcher.match("/exempted/**", "/non/exempted/path")).thenReturn(false);

        boolean result = nextCsrfValidator.isExemptedPath("/non/exempted/path");

        assertFalse(result);
    }
}