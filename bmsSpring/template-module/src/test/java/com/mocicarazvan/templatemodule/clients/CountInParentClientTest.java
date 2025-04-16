package com.mocicarazvan.templatemodule.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.clients.beans.CountInParentClientImpl;
import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class CountInParentClientTest {

    private MockWebServer mockWebServer;

    private CountInParentClient countInParentClient;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    private final RetryRegistry retryRegistry = RetryRegistry.of(
            RetryConfig.custom()
                    .maxAttempts(3)
                    .waitDuration(Duration.ofMillis(10))
                    .build());
    private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/api").toString();

        WebClient.Builder clientBuilder = WebClient.builder();

        countInParentClient = new CountInParentClientImpl("test-service", clientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
        countInParentClient.setServiceUrl(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @SneakyThrows
    void getCountInParent_success() {
        AtomicBoolean fbCalled = new AtomicBoolean(false);
        var ec = EntityCount.builder()
                .count(1L)
                .build();
        var body = objectMapper.writeValueAsString(ec);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", "application/x-ndjson")
                .setBody(body)
        );

        StepVerifier.create(countInParentClient.getCountInParent(1L, "1",
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return null;
                        }))
                .expectNext(ec)
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertFalse(fbCalled.get());
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/internal/count/1", recordedRequest.getPath());
        assertEquals("1", recordedRequest.getHeader(RequestsUtils.AUTH_HEADER));
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void getCountInParent_errorNoRecover() {
        AtomicBoolean fbCalled = new AtomicBoolean(false);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );

        StepVerifier.create(countInParentClient.getCountInParent(1L, "1",
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return Mono.error(new RuntimeException("Fallback error"));
                        }))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Fallback error"))
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertTrue(fbCalled.get());
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/internal/count/1", recordedRequest.getPath());
        assertEquals("1", recordedRequest.getHeader(RequestsUtils.AUTH_HEADER));
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(3, mockWebServer.getRequestCount());

    }

    @Test
    @SneakyThrows
    void getItemById_serviceUrlNull() {
        countInParentClient.setServiceUrl(null);

        StepVerifier.create(countInParentClient.getCountInParent(1L, "1",
                        (throwFallback) -> Mono.error(new RuntimeException("Fallback error"))))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Service url is null"))
                .verify();

        assertEquals(0, mockWebServer.getRequestCount());
    }


}