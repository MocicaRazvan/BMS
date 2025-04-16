package com.mocicarazvan.templatemodule.clients;

import com.mocicarazvan.templatemodule.dtos.errors.BaseErrorResponse;
import com.mocicarazvan.templatemodule.dtos.errors.IdNameResponse;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.exceptions.common.ServiceCallFailedException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.testUtils.AssertionTestUtils;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientExceptionHandlerTest {

    @Mock
    ClientResponse response;
    LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(ClientExceptionHandler.class);
        logCaptor.clearLogs();
    }

    @Test
    void handleClientExceptionWith404StatusShouldReturnNotFoundEntityException() {
        when(response.statusCode()).thenReturn(HttpStatus.NOT_FOUND);

        IdNameResponse idNameResponse = new IdNameResponse("entity", 123L);
        when(response.bodyToMono(IdNameResponse.class)).thenReturn(Mono.just(idNameResponse));
        var ex = new NotFoundEntity(idNameResponse.getName(), idNameResponse.getId());

        StepVerifier.create(ClientExceptionHandler.handleClientException(response, "/api/test", "testService"))
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundEntity &&
                                throwable.getMessage().equals(ex.getMessage()))
                .verify();
        assertLog();
    }

    private void assertLog() {
        await()
                .atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(() -> {
                    assertTrue(logCaptor.getErrorLogs().stream().anyMatch(log -> log.contains("Status code")));
                });
    }

    @Test
    void handleClientExceptionWith403StatusShouldReturnPrivateRouteException() {
        when(response.statusCode()).thenReturn(HttpStatus.FORBIDDEN);

        BaseErrorResponse errorResponse = new BaseErrorResponse();
        when(response.bodyToMono(BaseErrorResponse.class)).thenReturn(Mono.just(errorResponse));

        StepVerifier.create(ClientExceptionHandler.handleClientException(response, "/api/test", "testService"))
                .expectError(PrivateRouteException.class)
                .verify();
        assertLog();
    }

    @Test
    void handleClientExceptionWith400StatusShouldReturnIllegalActionException() {
        when(response.statusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        BaseErrorResponse errorResponse = new BaseErrorResponse();
        errorResponse.setMessage("Invalid request");
        when(response.bodyToMono(BaseErrorResponse.class)).thenReturn(Mono.just(errorResponse));

        StepVerifier.create(ClientExceptionHandler.handleClientException(response, "/api/test", "testService"))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalActionException &&
                                ((IllegalActionException) throwable).getMessage().equals(errorResponse.getMessage()))
                .verify();
        assertLog();
    }

    @Test
    void handleClientExceptionWith503StatusShouldReturnThrowFallback() {
        when(response.statusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);

        StepVerifier.create(ClientExceptionHandler.handleClientException(response, "/api/test", "testService"))
                .expectError(ThrowFallback.class)
                .verify();
        assertLog();
    }

    @Test
    void handleClientExceptionWith500StatusShouldReturnThrowFallback() {
        when(response.statusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        StepVerifier.create(ClientExceptionHandler.handleClientException(response, "/api/test", "testService"))
                .expectError(ThrowFallback.class)
                .verify();

        assertLog();
    }

    @Test
    void handleClientExceptionWithOtherStatusShouldReturnServiceCallFailedException() {
        when(response.statusCode()).thenReturn(HttpStatus.CONFLICT);

        when(response.bodyToMono(String.class)).thenReturn(Mono.just("Conflict occurred"));

        StepVerifier.create(ClientExceptionHandler.handleClientException(response, "/api/test", "testService"))
                .expectErrorMatches(throwable ->
                        throwable instanceof ServiceCallFailedException &&
                                ((ServiceCallFailedException) throwable).getServiceName().equals("testService") &&
                                ((ServiceCallFailedException) throwable).getServicePath().equals("/api/test"))
                .verify();

        assertLog();
    }

    @Test
    void handleWebRequestExceptionShouldWrapExceptionInThrowFallback() {
        Exception testException = new RuntimeException("Test exception");

        StepVerifier.create(ClientExceptionHandler.handleWebRequestException(testException))
                .expectError(ThrowFallback.class)
                .verify();

        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(() -> {
                    assertTrue(logCaptor.getErrorLogs().stream().anyMatch(log -> log.contains("Error:")));
                });
    }
}