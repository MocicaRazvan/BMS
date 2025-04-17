package com.mocicarazvan.gatewayservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.mockito.Mockito.*;


@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class NextJWEDecryptTest {
    @Spy
    private ObjectMapper mapper = new ObjectMapper();
    @Mock
    private DirectDecrypter mockDecrypter;

    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler() {
        @Override
        public void execute(Runnable task) {
            task.run();
        }
    };
    private NextJWEDecrypt decrypt;

    @BeforeEach
    void setUp() {
        taskScheduler.initialize();
        decrypt = new NextJWEDecrypt(mapper, mockDecrypter, taskScheduler);
    }

    @AfterEach
    void tearDown() {
        taskScheduler.shutdown();
    }

    @Test
    void shouldReturnDecryptedTokenForValidToken() throws Exception {
        String validToken = "valid-jwe-token";

        String decryptedJson = """
                {
                    "user": {
                        "token": "expected-token",
                        "id": 1,
                        "firstName": "Test",
                        "lastName": "User",
                        "email": "test@example.com",
                        "role": "admin",
                        "image": "image.png",
                        "provider": "credentials",
                        "emailVerified": true
                    }
                }
                """;


        JWEObject mockJweObject = mock(JWEObject.class);
        Payload realPayload = new Payload(decryptedJson);

        try (MockedStatic<JWEObject> jweStatic = mockStatic(JWEObject.class)) {
            jweStatic.when(() -> JWEObject.parse(validToken)).thenReturn(mockJweObject);

            when(mockJweObject.getPayload()).thenReturn(realPayload);
            doNothing().when(mockJweObject).decrypt(mockDecrypter);

            StepVerifier.create(decrypt.getTokenPayload(validToken))
                    .expectNext("expected-token")
                    .verifyComplete();

            verify(mockJweObject).decrypt(mockDecrypter);
        }
    }

    @Test
    void shouldReturnNullCookieForNullToken() {
        StepVerifier.create(decrypt.getTokenPayload(null))
                .expectNext(NextJWEDecrypt.NULL_COOKIE)
                .verifyComplete();
    }

    @Test
    void shouldReturnNullCookieForBlankToken() {
        StepVerifier.create(decrypt.getTokenPayload("   "))
                .expectNext(NextJWEDecrypt.NULL_COOKIE)
                .verifyComplete();
    }

    @Test
    void shouldReturnNullCookieWhenTokenParseFails() {
        String badToken = "malformed-token";

        try (MockedStatic<JWEObject> jweStatic = mockStatic(JWEObject.class)) {
            jweStatic.when(() -> JWEObject.parse(badToken))
                    .thenThrow(new RuntimeException("parse failed"));

            StepVerifier.create(decrypt.getTokenPayload(badToken))
                    .expectNext(NextJWEDecrypt.NULL_COOKIE)
                    .verifyComplete();
        }
    }

    @Test
    void shouldReturnNullCookieWhenDecryptionFails() throws Exception {
        String token = "token-to-decrypt";

        JWEObject mockJweObject = mock(JWEObject.class);

        try (MockedStatic<JWEObject> jweStatic = mockStatic(JWEObject.class)) {
            jweStatic.when(() -> JWEObject.parse(token)).thenReturn(mockJweObject);
            doThrow(new RuntimeException("decryption failed")).when(mockJweObject).decrypt(mockDecrypter);


            StepVerifier.create(decrypt.getTokenPayload(token))
                    .expectNext(NextJWEDecrypt.NULL_COOKIE)
                    .verifyComplete();
        }
    }

    @Test
    void shouldReturnNullCookieWhenDeserializationFails() throws Exception {
        String token = "valid-token";
        byte[] badPayload = "{\"badJson\": true}".getBytes(StandardCharsets.UTF_8);

        JWEObject mockJweObject = mock(JWEObject.class);
        Payload mockPayload = mock(Payload.class);

        try (MockedStatic<JWEObject> jweStatic = mockStatic(JWEObject.class)) {
            jweStatic.when(() -> JWEObject.parse(token)).thenReturn(mockJweObject);
            when(mockJweObject.getPayload()).thenReturn(mockPayload);
            when(mockPayload.toBytes()).thenReturn(badPayload);
            doNothing().when(mockJweObject).decrypt(mockDecrypter);


            StepVerifier.create(decrypt.getTokenPayload(token))
                    .expectNext(NextJWEDecrypt.NULL_COOKIE)
                    .verifyComplete();
        }
    }

    @Test
    void benchmarkRealTokenDecryption() throws Exception {
        byte[] sharedSecret = new byte[32];
        new SecureRandom().nextBytes(sharedSecret);

        String jsonPayload = """
                {
                    "user": {
                        "token": "benchmark-token",
                        "id": 1,
                        "firstName": "Test",
                        "lastName": "User",
                        "email": "test@example.com",
                        "role": "admin",
                        "image": "image.png",
                        "provider": "credentials",
                        "emailVerified": true
                    }
                }
                """;

        Payload payload = new Payload(jsonPayload);
        JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A256GCM);
        JWEObject jweObject = new JWEObject(header, payload);

        jweObject.encrypt(new DirectEncrypter(sharedSecret));
        String jweString = jweObject.serialize();

        ObjectMapper realMapper = new ObjectMapper();
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();

        DirectDecrypter realDecrypter = new DirectDecrypter(sharedSecret);
        NextJWEDecrypt decrypt = new NextJWEDecrypt(realMapper, realDecrypter, scheduler);

        long start = System.nanoTime();

        StepVerifier.create(decrypt.getTokenPayload(jweString))
                .expectNext("benchmark-token")
                .verifyComplete();

        long end = System.nanoTime();
        long durationMs = (end - start) / 1_000_000;

        log.info("Decryption took {} ms", durationMs);
    }

}