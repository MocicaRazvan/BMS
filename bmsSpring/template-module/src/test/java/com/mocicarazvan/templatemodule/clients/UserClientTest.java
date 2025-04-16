package com.mocicarazvan.templatemodule.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.common.ServiceCallFailedException;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class UserClientTest {
    private MockWebServer mockWebServer;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    private final RetryRegistry retryRegistry = RetryRegistry.of(
            RetryConfig.custom()
                    .maxAttempts(3)
                    .waitDuration(Duration.ofMillis(10))
                    .build());
    private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
    private UserClient userClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/api").toString();

        WebClient.Builder clientBuilder = WebClient.builder();

        userClient = new UserClient("user-service", clientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
        ReflectionTestUtils.setField(userClient, "userServiceUrl", baseUrl);
        userClient.setServiceUrl(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @SneakyThrows
    void handleNotFoundException_notFound() {
        NotFoundEntity notFound = new NotFoundEntity("User", 1L);
        String body = new ObjectMapper().writeValueAsString(notFound);
        ClientResponse response = ClientResponse
                .create(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build();
        Mono<?> result = ReflectionTestUtils.invokeMethod(
                userClient,
                "handleNotFoundException",
                response,
                "/users/1"
        );
        assertNotNull(result);
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundEntity)
                .verify();
    }

    @Test
    @SneakyThrows
    void handleNotFoundException_other() {
        ClientResponse response = ClientResponse
                .create(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("Other error")
                .build();
        Mono<?> result = ReflectionTestUtils.invokeMethod(
                userClient,
                "handleNotFoundException",
                response,
                "/users/1"
        );
        assertNotNull(result);
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ServiceCallFailedException)
                .verify();
    }

    @Test
    @SneakyThrows
    void getUser_success() {
        var user = UserDto.builder()
                .id(1L)
                .build();
        var body = objectMapper.writeValueAsString(CustomEntityModel.of(user));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", "application/x-ndjson")
                .setBody(body)
        );

        StepVerifier.create(userClient.getUser(""))
                .expectNext(user)
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/users", recordedRequest.getPath());
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void getUser_notFound() {
        var ex = new NotFoundEntity("user", 1L);
        var body = objectMapper.writeValueAsString(ex);
        mockWebServer.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/x-ndjson")
                .setResponseCode(HttpStatus.NOT_FOUND.value())
        );
        mockWebServer.enqueue(new MockResponse().setBody(body)
                .setHeader("Content-Type", "application/x-ndjson")
                .setResponseCode(HttpStatus.NOT_FOUND.value())
        );
        mockWebServer.enqueue(new MockResponse().setBody(body)
                .setHeader("Content-Type", "application/x-ndjson")
                .setResponseCode(HttpStatus.NOT_FOUND.value())
        );

        StepVerifier.create(userClient.getUser(""))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundEntity
                        && ((NotFoundEntity) throwable).getName().equals("user")
                        && ((NotFoundEntity) throwable).getId() == 1L
                )
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/users", recordedRequest.getPath());
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(3, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void getUser_otherError() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("other error")
                .setHeader("Content-Type", "application/x-ndjson")
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        mockWebServer.enqueue(new MockResponse().setBody("other error")
                .setHeader("Content-Type", "application/x-ndjson")
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        mockWebServer.enqueue(new MockResponse().setBody("other error")
                .setHeader("Content-Type", "application/x-ndjson")
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        StepVerifier.create(userClient.getUser(""))
                .expectErrorMatches(throwable -> throwable instanceof ServiceCallFailedException
                        && throwable.getMessage().equals("other error")
                )
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/users", recordedRequest.getPath());
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(3, mockWebServer.getRequestCount());
    }

    @Test
    void hasPermissionToModify_sameUser() {
        StepVerifier.create(userClient.hasPermissionToModifyEntity(UserDto.builder().id(1L).build(), 1L))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void hasPermissionToModify_admin() {
        StepVerifier.create(userClient.hasPermissionToModifyEntity(UserDto.builder().role(Role.ROLE_ADMIN).build(), 1L))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void hasPermissionToModify_notPermitted() {
        StepVerifier.create(userClient.hasPermissionToModifyEntity(UserDto.builder().id(2L).role(Role.ROLE_TRAINER).build(), 1L))
                .expectNext(false)
                .verifyComplete();
    }

    @ParameterizedTest
    @SneakyThrows
    @NullAndEmptySource
    void existsUser_successRolesEmpty(List<Role> roles) {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NO_CONTENT.value())
        );

        StepVerifier.create(userClient.existsUser("", roles))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/users", recordedRequest.getPath());
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());

        assertNotNull(recordedRequest.getRequestUrl());
        assertEquals(0, recordedRequest.getRequestUrl().queryParameterValues("roles").size());

    }

    @Test
    @SneakyThrows
    void existsUser_successRolesPresent() {
        var roles = Arrays.stream(Role.values()).toList();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NO_CONTENT.value())
        );

        StepVerifier.create(userClient.existsUser("", roles))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertNotNull(recordedRequest.getRequestUrl());
        assertEquals("/api/users", recordedRequest.getRequestUrl().encodedPath());
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());

        assertNotNull(recordedRequest.getRequestUrl());
        assertEquals(roles.size(), recordedRequest.getRequestUrl().queryParameterValues("roles").size());

    }

    @Test
    @SneakyThrows
    void getUsersByIdIn_success() {
        var user = UserDto.builder()
                .id(1L)
                .build();
        var body = objectMapper.writeValueAsString(CustomEntityModel.of(user));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", "application/x-ndjson")
                .setBody(body)
        );

        StepVerifier.create(userClient.getUsersByIdIn("", List.of(1L)))
                .expectNext(user)
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertNotNull(recordedRequest.getRequestUrl());
        assertEquals("/api/users", recordedRequest.getRequestUrl().encodedPath());
        assertEquals("application/x-ndjson", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    @SneakyThrows
    void getUsersByIdIn_successEmptyIds() {

        StepVerifier.create(userClient.getUsersByIdIn("", List.of()))
                .verifyComplete();

        assertEquals(0, mockWebServer.getRequestCount());
    }

}