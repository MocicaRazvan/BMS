package com.mocicarazvan.templatemodule.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.exceptions.common.WrappingMonoException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
public class ObjectMapperMonoWrapper {

    private final ObjectMapper objectMapper;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    public ObjectMapperMonoWrapper(ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
        this.objectMapper = new ObjectMapper();
    }

    public <T> Mono<String> wrapBlockingFunction(T object) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(object))
                .onErrorMap(e -> new WrappingMonoException("Error during writing function execution", e))
                .subscribeOn(Schedulers.fromExecutor(threadPoolTaskScheduler));
    }

}
