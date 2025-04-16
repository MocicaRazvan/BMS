package com.mocicarazvan.templatemodule.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.clients.beans.ValidIdsClientImpl;
import com.mocicarazvan.templatemodule.dtos.WithUserDtoImpl;
import com.mocicarazvan.templatemodule.testUtils.ClientTestUtils;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class ValidIdsClientTest {
    private MockWebServer mockWebServer;
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    private final RetryRegistry retryRegistry = RetryRegistry.of(
            RetryConfig.custom()
                    .maxAttempts(3)
                    .waitDuration(Duration.ofMillis(10))
                    .build());
    private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();

    private ValidIdsClient<WithUserDtoImpl> validIdsClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/api").toString();

        WebClient.Builder clientBuilder = WebClient.builder();

        validIdsClient = new ValidIdsClientImpl("test-service", clientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
        validIdsClient.setServiceUrl(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @SneakyThrows
    void verifyIds_success() {
        AtomicBoolean fbCalled = new AtomicBoolean(false);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NO_CONTENT.value())
        );
        var ids = List.of("1", "1", "2");

        StepVerifier.create(validIdsClient.verifyIds(ids, "1",
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return null;
                        }))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertFalse(fbCalled.get());
        assertEquals("GET", recordedRequest.getMethod());
        assertNotNull(recordedRequest.getRequestUrl());
        assertEquals("/api/internal/validIds", recordedRequest.getRequestUrl().encodedPath());
        assertEquals("1", recordedRequest.getHeader(RequestsUtils.AUTH_HEADER));
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());
        assertNotNull(recordedRequest.getRequestUrl());
        assertEquals(2, recordedRequest.getRequestUrl().queryParameterValues("ids").size());
    }

    @Test
    @SneakyThrows
    void verifyIds_idsEmpty() {
        AtomicBoolean fbCalled = new AtomicBoolean(false);


        StepVerifier.create(validIdsClient.verifyIds(List.of(), "1",
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return null;
                        }))
                .verifyComplete();

        assertFalse(fbCalled.get());
        assertEquals(0, mockWebServer.getRequestCount());

    }

    @Test
    @SneakyThrows
    void verifyIds_serviceUrlNull() {
        validIdsClient.setServiceUrl(null);
        AtomicBoolean fbCalled = new AtomicBoolean(false);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NO_CONTENT.value())
        );
        var ids = List.of("1", "1", "2");

        StepVerifier.create(validIdsClient.verifyIds(ids, "1",
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return null;
                        }))
                .expectError(IllegalArgumentException.class)
                .verify();

        assertFalse(fbCalled.get());
        assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void verifyIds_error() {
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
        var ids = List.of("1", "1", "2");

        StepVerifier.create(validIdsClient.verifyIds(ids, "1",
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return Mono.error(new RuntimeException("Fallback error"));
                        }))
                .expectErrorMatches(throwable -> {
                    assertInstanceOf(RuntimeException.class, throwable);
                    assertEquals("Fallback error", throwable.getMessage());
                    return true;
                })
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertTrue(fbCalled.get());
        assertEquals("GET", recordedRequest.getMethod());
        assertNotNull(recordedRequest.getRequestUrl());
        assertEquals("/api/internal/validIds", recordedRequest.getRequestUrl().encodedPath());
        assertEquals("1", recordedRequest.getHeader(RequestsUtils.AUTH_HEADER));
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(3, mockWebServer.getRequestCount());
        assertNotNull(recordedRequest.getRequestUrl());
        assertEquals(2, recordedRequest.getRequestUrl().queryParameterValues("ids").size());
    }

    @Test
    @SneakyThrows
    void getByIds_success() {
        AtomicBoolean fbCalled = new AtomicBoolean(false);
        var dto = new WithUserDtoImpl();
        var items = List.of(dto, dto);
        var ids = List.of("1", "1", "2");
        var ndjson = ClientTestUtils.fromCollectionToNdjson(items);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", "application/x-ndjson")
                .setBody(ndjson)
        );

        StepVerifier.create(validIdsClient.getByIds(ids, "1", WithUserDtoImpl.class,
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return null;
                        }))
                .expectNextCount(2)
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertFalse(fbCalled.get());
        assertEquals("GET", recordedRequest.getMethod());
        assertNotNull(recordedRequest.getRequestUrl());
        assertEquals("/api/internal/getByIds", recordedRequest.getRequestUrl().encodedPath());
        assertEquals("1", recordedRequest.getHeader(RequestsUtils.AUTH_HEADER));
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());
        assertNotNull(recordedRequest.getRequestUrl());
        assertEquals(2, recordedRequest.getRequestUrl().queryParameterValues("ids").size());
    }

    @Test
    @SneakyThrows
    void getByIds_idsEmpty() {
        AtomicBoolean fbCalled = new AtomicBoolean(false);


        StepVerifier.create(validIdsClient.getByIds(List.of(), "1", WithUserDtoImpl.class,
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return null;
                        }))
                .verifyComplete();

        assertFalse(fbCalled.get());
        assertEquals(0, mockWebServer.getRequestCount());

    }

    @Test
    @SneakyThrows
    void getByIds_serviceUrlNull() {
        validIdsClient.setServiceUrl(null);
        AtomicBoolean fbCalled = new AtomicBoolean(false);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NO_CONTENT.value())
        );
        var ids = List.of("1", "1", "2");

        StepVerifier.create(validIdsClient.getByIds(ids, "1", WithUserDtoImpl.class,
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return null;
                        }))
                .expectError(IllegalArgumentException.class)
                .verify();

        assertFalse(fbCalled.get());
        assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void getByIds_error() {
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
        var ids = List.of("1", "1", "2");

        StepVerifier.create(validIdsClient.getByIds(ids, "1", WithUserDtoImpl.class,
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return Flux.error(new RuntimeException("Fallback error"));
                        }))
                .expectErrorMatches(throwable -> {
                    assertInstanceOf(RuntimeException.class, throwable);
                    assertEquals("Fallback error", throwable.getMessage());
                    return true;
                })
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertTrue(fbCalled.get());
        assertEquals("GET", recordedRequest.getMethod());
        assertNotNull(recordedRequest.getRequestUrl());
        assertEquals("/api/internal/getByIds", recordedRequest.getRequestUrl().encodedPath());
        assertEquals("1", recordedRequest.getHeader(RequestsUtils.AUTH_HEADER));
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(3, mockWebServer.getRequestCount());
        assertNotNull(recordedRequest.getRequestUrl());
        assertEquals(2, recordedRequest.getRequestUrl().queryParameterValues("ids").size());
    }

}