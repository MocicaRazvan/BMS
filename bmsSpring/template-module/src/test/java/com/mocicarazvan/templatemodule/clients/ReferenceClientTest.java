package com.mocicarazvan.templatemodule.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.clients.beans.ReferenceClientImpl;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
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
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(SpringExtension.class)
class ReferenceClientTest {
    private MockWebServer mockWebServer;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    private final RetryRegistry retryRegistry = RetryRegistry.of(
            RetryConfig.custom()
                    .maxAttempts(3)
                    .waitDuration(Duration.ofMillis(10))
                    .build());
    private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();

    private ReferenceClient referenceClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/api").toString();

        WebClient.Builder clientBuilder = WebClient.builder();

        referenceClient = new ReferenceClientImpl("test-service", clientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry, "test-reference");
        referenceClient.setServiceUrl(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @SneakyThrows
    void existsApprovedReference_success() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NO_CONTENT.value())
        );

        StepVerifier.create(referenceClient.existsApprovedReference("1"))
                .expectSubscription()
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/test-references/internal/existsApproved/1", recordedRequest.getPath());
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void existsApprovedReference_error() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        var ex = new NotFoundEntity("test-reference", 1L);
        StepVerifier.create(referenceClient.existsApprovedReference("1"))
                .expectErrorMatches(throwable -> {
                    assertInstanceOf(NotFoundEntity.class, throwable);
                    assertEquals(ex.getMessage(), throwable.getMessage());
                    return true;
                })
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/test-references/internal/existsApproved/1", recordedRequest.getPath());
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(3, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void existsApprovedReference_serviceUrlNull() {
        referenceClient.setServiceUrl(null);
        var ex = new NotFoundEntity("test-reference", 1L);
        StepVerifier.create(referenceClient.existsApprovedReference("1"))
                .expectErrorMatches(throwable -> {
                    assertInstanceOf(NotFoundEntity.class, throwable);
                    assertEquals(ex.getMessage(), throwable.getMessage());
                    return true;
                })
                .verify();

        assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void getReferenceById_success() {
        var res = ApproveDto.builder()
                .id(1L)
                .approved(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .userId(1L)
                .userDislikes(List.of())
                .userDislikes(List.of())
                .title("title")
                .body("body")
                .build();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(objectMapper.writeValueAsString(CustomEntityModel.of(res)))
                .addHeader("Content-Type", "application/x-ndjson")
        );

        StepVerifier.create(referenceClient.getReferenceById("1", "1", ApproveDto.class))
                .expectNext(res)
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/test-references/1", recordedRequest.getPath());
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void getReferenceById_error() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        var ex = new NotFoundEntity("test-reference", 1L);
        StepVerifier.create(referenceClient.getReferenceById("1", "1", ApproveDto.class))
                .expectErrorMatches(throwable -> {
                    assertInstanceOf(NotFoundEntity.class, throwable);
                    assertEquals(ex.getMessage(), throwable.getMessage());
                    return true;
                })
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/test-references/1", recordedRequest.getPath());
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(3, mockWebServer.getRequestCount());
    }
}