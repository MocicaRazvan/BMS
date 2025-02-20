package com.mocicarazvan.templatemodule.clients;

import com.mocicarazvan.templatemodule.dtos.errors.BaseErrorResponse;
import com.mocicarazvan.templatemodule.dtos.errors.IdNameResponse;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.exceptions.common.ServiceCallFailedException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@Slf4j
public class ClientExceptionHandler {

    public static Mono<? extends Throwable> handleClientException(ClientResponse response, String uri, String service) {
        log.error("Status code: {}, uri: {}", response.statusCode(), uri);
        if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
            return response
                    .bodyToMono(IdNameResponse.class)
                    .log()
                    .flatMap(idNameResponse -> Mono.error(new NotFoundEntity(idNameResponse.getName(), idNameResponse.getId())));
        } else if (response.statusCode().equals(HttpStatus.FORBIDDEN)) {
            return response.bodyToMono(BaseErrorResponse.class)
                    .flatMap(baseErrorResponse -> Mono.error(new PrivateRouteException()));
        } else if (response.statusCode().equals(HttpStatus.BAD_REQUEST)) {
            return response.bodyToMono(BaseErrorResponse.class)
                    .flatMap(baseErrorResponse -> Mono.error(new IllegalActionException(baseErrorResponse.getMessage())));
        } else if (response.statusCode().equals(HttpStatus.SERVICE_UNAVAILABLE) || response.statusCode().is5xxServerError()) {
            return Mono.error(new ThrowFallback());
        } else {
            return response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new ServiceCallFailedException(body, service, uri)));
        }


    }

    public static <T> Mono<T> handleWebRequestException(Throwable e) {
        log.error("Error: ", e);
        return Mono.error(new ThrowFallback());
    }

}
