package com.mocicarazvan.templatemodule.utils;

import com.mocicarazvan.templatemodule.exceptions.common.WrappingMonoException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


public class MonoWrapper {

    public static void wrapBlockingFunction(Runnable blockingFunction) {
        Mono.fromCallable(() -> {
                    blockingFunction.run();
                    return true;
                })
                .doOnError(e -> {
                    throw new WrappingMonoException("Error during blocking function execution", e);
                })
                .subscribeOn(Schedulers.boundedElastic()).subscribe();
    }
}
