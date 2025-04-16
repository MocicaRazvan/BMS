package com.mocicarazvan.ollamasearch.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.ollamasearch.config.OllamaPropertiesConfig;
import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedRequestModel;
import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedResponseModel;
import com.mocicarazvan.ollamasearch.exceptions.OllamaEmbedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
class OllamaAPITest {
    private MockWebServer mockWebServer;

    private OllamaAPI ollamaAPI;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    private final RetryRegistry retryRegistry = RetryRegistry.of(
            RetryConfig.custom()
                    .maxAttempts(3)
                    .waitDuration(Duration.ofMillis(10))
                    .build());
    private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();

    private OllamaPropertiesConfig ollamaPropertiesConfig;

    private final OllamaEmbedRequestModel ollamaEmbedRequestModel = new OllamaEmbedRequestModel();


    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString();
        ollamaPropertiesConfig = new OllamaPropertiesConfig();
        ollamaPropertiesConfig.setUrl(baseUrl);
        WebClient.Builder clientBuilder = WebClient.builder();

        ollamaAPI = new OllamaAPI(ollamaPropertiesConfig, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry, clientBuilder);

    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @Order(1)
    void loads() {
        assertNotNull(ollamaAPI);
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:11434/", "http://localhost:11434"})
    void testHandleUrl(String url) {
        var properties = new OllamaPropertiesConfig();
        properties.setUrl(url);
        var expected = "http://localhost:11434";

        var actual = ReflectionTestUtils.invokeMethod(
                ollamaAPI,
                "handleUrl",
                properties
        );

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    void embed_success() {
        var model = "bge-m3";
        var embedding = List.of(List.of(1.0));
        var resp = OllamaEmbedResponseModel.builder()
                .model(model)
                .embeddings(embedding)
                .build();
        var body = objectMapper.writeValueAsString(resp);
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody(body)
        );

        StepVerifier.create(ollamaAPI.embed(ollamaEmbedRequestModel))
                .expectNext(resp)
                .verifyComplete();

        var request = mockWebServer.takeRequest();

        assertEquals("POST", request.getMethod());
        assertNotNull(request.getRequestUrl());
        assertEquals("/api/embed", request.getRequestUrl().encodedPath());
        assertEquals("application/json", request.getHeader("Accept"));
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "UNAUTHORIZED", "NOT_FOUND", "INTERNAL_SERVER_ERROR", "CREATED"})
    void embed_notSuccess_noRecovery(HttpStatus status) {
        for (int i = 0; i < retryRegistry.getDefaultConfig().getMaxAttempts(); i++) {
            mockWebServer.enqueue(
                    new MockResponse()
                            .setResponseCode(status.value())
                            .setBody("Error " + status.getReasonPhrase())
            );
        }
        var ex = new OllamaEmbedException(status + " _ " + "Error " + status.getReasonPhrase());

        StepVerifier.create(ollamaAPI.embed(ollamaEmbedRequestModel))
                .expectErrorMatches(
                        throwable -> throwable instanceof OllamaEmbedException &&
                                throwable.getMessage().equals(ex.getMessage())
                )
                .verify();

    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "UNAUTHORIZED", "NOT_FOUND", "INTERNAL_SERVER_ERROR", "CREATED"})
    @SneakyThrows
    void embed_notSuccess_recovery(HttpStatus status) {
        for (int i = 0; i < retryRegistry.getDefaultConfig().getMaxAttempts() - 1; i++) {
            mockWebServer.enqueue(
                    new MockResponse()
                            .setResponseCode(status.value())
                            .setBody("Error " + status.getReasonPhrase())
            );
        }
        var model = "bge-m3";
        var embedding = List.of(List.of(1.0));
        var resp = OllamaEmbedResponseModel.builder()
                .model(model)
                .embeddings(embedding)
                .build();
        var body = objectMapper.writeValueAsString(resp);
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody(body)
        );

        StepVerifier.create(ollamaAPI.embed(ollamaEmbedRequestModel))
                .expectNext(resp)
                .verifyComplete();

        var request = mockWebServer.takeRequest();

        assertEquals("POST", request.getMethod());
        assertNotNull(request.getRequestUrl());
        assertEquals("/api/embed", request.getRequestUrl().encodedPath());
        assertEquals("application/json", request.getHeader("Accept"));
    }
}