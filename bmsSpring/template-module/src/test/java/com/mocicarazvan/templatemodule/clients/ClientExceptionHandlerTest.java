package com.mocicarazvan.templatemodule.clients;

import com.mocicarazvan.templatemodule.dtos.errors.BaseErrorResponse;
import com.mocicarazvan.templatemodule.dtos.errors.IdNameResponse;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.exceptions.common.ServiceCallFailedException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientExceptionHandlerTest {

    @Mock
    ClientResponse response;

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
    }

    @Test
    void handleClientExceptionWith403StatusShouldReturnPrivateRouteException() {
        when(response.statusCode()).thenReturn(HttpStatus.FORBIDDEN);

        BaseErrorResponse errorResponse = new BaseErrorResponse();
        when(response.bodyToMono(BaseErrorResponse.class)).thenReturn(Mono.just(errorResponse));

        StepVerifier.create(ClientExceptionHandler.handleClientException(response, "/api/test", "testService"))
                .expectError(PrivateRouteException.class)
                .verify();
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
    }

    @Test
    void handleClientExceptionWith503StatusShouldReturnThrowFallback() {
        when(response.statusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);

        StepVerifier.create(ClientExceptionHandler.handleClientException(response, "/api/test", "testService"))
                .expectError(ThrowFallback.class)
                .verify();
    }

    @Test
    void handleClientExceptionWith500StatusShouldReturnThrowFallback() {
        when(response.statusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        StepVerifier.create(ClientExceptionHandler.handleClientException(response, "/api/test", "testService"))
                .expectError(ThrowFallback.class)
                .verify();

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

    }

    @Test
    void handleWebRequestExceptionShouldWrapExceptionInThrowFallback() {
        Exception testException = new RuntimeException("Test exception");

        StepVerifier.create(ClientExceptionHandler.handleWebRequestException(testException))
                .expectError(ThrowFallback.class)
                .verify();

    }
}