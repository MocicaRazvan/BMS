package com.mocicarazvan.templatemodule.utils;

import com.mocicarazvan.templatemodule.exceptions.common.WrappingMonoException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Slf4j
public class MonoWrapper {

    public static void wrapBlockingFunction(Runnable blockingFunction) {
        wrapBlockingFunction(blockingFunction, null);
    }

    public static void wrapBlockingFunction(Runnable blockingFunction, Retry retrySpec) {
        Mono.fromCallable(() -> {
                    try {
                        blockingFunction.run();
                        return true;
                    } catch (Exception e) {
                        log.error("Error in blocking function: {}", e.getMessage(), e);
                        throw new WrappingMonoException("Error during blocking function execution", e);
                    }
                })
                .transform(mono -> retrySpec != null ? mono.retryWhen(retrySpec) : mono)
                .subscribeOn(Schedulers.boundedElastic()).subscribe(
                        success -> {
                        },
                        error -> log.error("Error in blocking function: {}", error.getMessage(), error)
                );
    }
}
