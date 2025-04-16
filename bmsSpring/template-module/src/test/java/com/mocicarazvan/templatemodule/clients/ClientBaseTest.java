package com.mocicarazvan.templatemodule.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.clients.beans.ClientBaseImpl;
import com.mocicarazvan.templatemodule.models.IdGeneratedImpl;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class ClientBaseTest {
    private MockWebServer mockWebServer;

    private ClientBase clientBase;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final RequestsUtils requestsUtils = new RequestsUtils();

    private final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    private final RetryRegistry retryRegistry = RetryRegistry.of(
            RetryConfig.custom()
                    .maxAttempts(3)
                    .waitDuration(Duration.ofMillis(10))
                    .build());
    private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
    private final List<IdGeneratedImpl> items = List.of(
            IdGeneratedImpl.builder().id(1L).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            IdGeneratedImpl.builder().id(2L).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            IdGeneratedImpl.builder().id(3L).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
    );

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/api").toString();

        WebClient.Builder clientBuilder = WebClient.builder();

        clientBase = new ClientBaseImpl(clientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
        clientBase.setServiceUrl(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @SneakyThrows
    void getItemById_success() {
        AtomicBoolean fbCalled = new AtomicBoolean(false);
        var body = objectMapper.writeValueAsString(items.getFirst());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", "application/x-ndjson")
                .setBody(body)
        );

        StepVerifier.create(clientBase.getItemById("1", "1", new ParameterizedTypeReference<IdGeneratedImpl>() {
                        }, "/item",
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return null;
                        }))
                .expectNext(items.getFirst())
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertFalse(fbCalled.get());
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/item/1", recordedRequest.getPath());
        assertEquals("1", recordedRequest.getHeader(RequestsUtils.AUTH_HEADER));
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());

    }

    @Test
    @SneakyThrows
    void getItemById_errorNoRecover() {
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

        StepVerifier.create(clientBase.getItemById("1", "1", new ParameterizedTypeReference<IdGeneratedImpl>() {
                        }, "/item",
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return Mono.error(new RuntimeException("Fallback error"));
                        }))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Fallback error"))
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertTrue(fbCalled.get());
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/item/1", recordedRequest.getPath());
        assertEquals("1", recordedRequest.getHeader(RequestsUtils.AUTH_HEADER));
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(3, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void getItemById_errorRecover() {
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

        StepVerifier.create(clientBase.getItemById("1", "1", new ParameterizedTypeReference<IdGeneratedImpl>() {
                        }, "/item",
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return Mono.error(new RuntimeException("Fallback error"));
                        }))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Fallback error"))
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertTrue(fbCalled.get());
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/item/1", recordedRequest.getPath());
        assertEquals("1", recordedRequest.getHeader(RequestsUtils.AUTH_HEADER));
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(3, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void getItemById_serviceUrlNull() {
        clientBase.setServiceUrl(null);

        StepVerifier.create(clientBase.getItemById("1", "1", new ParameterizedTypeReference<IdGeneratedImpl>() {
                        }, "/item",
                        (throwFallback) -> Mono.error(new RuntimeException("Fallback error"))))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Service url is null"))
                .verify();

        assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void getBaseFlux() {
        AtomicBoolean fbCalled = new AtomicBoolean(false);
        var ndjson = ClientTestUtils.fromCollectionToNdjson(items);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", "application/x-ndjson")
                .setBody(ndjson)
        );

        StepVerifier.create(clientBase.getBaseFlux("1", uriBuilder -> uriBuilder.path("/items").build(), new ParameterizedTypeReference<IdGeneratedImpl>() {
                        },
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return Flux.error(new RuntimeException("Fallback error"));
                        }))
                .expectNextSequence(items)
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertFalse(fbCalled.get());
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/items", recordedRequest.getPath());
        assertEquals("1", recordedRequest.getHeader(RequestsUtils.AUTH_HEADER));
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void getBaseFlux_serviceUrlNull() {
        clientBase.setServiceUrl(null);

        StepVerifier.create(clientBase.getBaseFlux("1", uriBuilder -> uriBuilder.path("/items").build(), new ParameterizedTypeReference<IdGeneratedImpl>() {
                        },
                        (throwFallback) -> {
                            return Flux.error(new RuntimeException("Fallback error"));
                        }))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Service url is null"))
                .verify();

        assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void getBaseMono_success() {
        AtomicBoolean fbCalled = new AtomicBoolean(false);
        var body = objectMapper.writeValueAsString(items.getFirst());
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", "application/x-ndjson")
                .setBody(body)
        );

        StepVerifier.create(clientBase.getBaseMono("1", uriBuilder -> uriBuilder.path("/item/1").build(), new ParameterizedTypeReference<IdGeneratedImpl>() {
                        },
                        (throwFallback) -> {
                            fbCalled.set(true);
                            return Mono.error(new RuntimeException("Fallback error"));
                        }))
                .expectNext(items.getFirst())
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertFalse(fbCalled.get());
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/item/1", recordedRequest.getPath());
        assertEquals("1", recordedRequest.getHeader(RequestsUtils.AUTH_HEADER));
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
    }

    @Test
    @SneakyThrows
    void getBaseMono_serviceUrlNull() {
        clientBase.setServiceUrl(null);

        StepVerifier.create(clientBase.getBaseMono("1", uriBuilder -> uriBuilder.path("/item/1").build(), new ParameterizedTypeReference<IdGeneratedImpl>() {
                        },
                        (throwFallback) -> {
                            return Mono.error(new RuntimeException("Fallback error"));
                        }))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Service url is null"))
                .verify();

        assertEquals(0, mockWebServer.getRequestCount());
    }

}