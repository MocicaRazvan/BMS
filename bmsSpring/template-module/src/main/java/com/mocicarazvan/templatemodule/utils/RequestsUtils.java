package com.mocicarazvan.templatemodule.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.exceptions.notFound.AuthHeaderNotFound;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

public class RequestsUtils {
    public static final String AUTH_HEADER = "x-auth-user-id";

    public String extractAuthUser(ServerWebExchange exchange) {
        return Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst(AUTH_HEADER)
        ).orElseThrow(AuthHeaderNotFound::new);
    }

    public <T> Mono<T> getBodyFromJson(String jsonBody, Class<T> clazz, ObjectMapper objectMapper, ThreadPoolTaskScheduler taskScheduler) {

        return Mono.just(jsonBody)
                .flatMap(jb -> Mono.fromCallable(() -> objectMapper.readValue(jb, clazz))
                        .subscribeOn(Schedulers.fromExecutor(taskScheduler.getScheduledExecutor())));

    }

    public List<Object> getListOfNotNullObjects(Object... objects) {
        return Arrays.stream(objects)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }


}
