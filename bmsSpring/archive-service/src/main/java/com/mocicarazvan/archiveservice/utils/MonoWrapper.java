package com.mocicarazvan.archiveservice.utils;

import com.mocicarazvan.archiveservice.exceptions.WrappingMonoException;
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
