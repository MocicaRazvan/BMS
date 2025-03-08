package com.mocicarazvan.cartservice.config;

import com.mocicarazvan.cartservice.services.UserCartService;
import com.mocicarazvan.cartservice.utils.CronUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TasksConfig {
    private final UserCartService userCartService;

    @Value("${cart.clear-old-carts.cron:0 0 0 * * *}")
    private String clearOldCartsCron;

    @Value("${cart.clear-old-carts.days-cutoff:60}")
    private Long clearOldCartsDaysCutoff;

    @Value("${cart.clear-old-carts.enabled:true}")
    private boolean isClearEnabled;


    @Bean
    public Disposable clearOldCartsTask() {

        if (!isClearEnabled) {
            log.info("Clear old carts task is disabled");
            return Flux.never().subscribe();
        }

        Pair<Duration, Duration> durations = CronUtils.computeDurations(clearOldCartsCron, LocalDateTime.now());
        return Flux.interval(durations.getFirst(), durations.getSecond())
                .concatMap(_ -> {
                    log.info("Clearing old carts");
                    return
                            userCartService.clearOldCarts(clearOldCartsDaysCutoff);
                })
                .subscribe(
                        result -> log.info("Old carts cleared: {}", result),
                        error -> log.error("Error clearing old carts", error),
                        () -> log.info("Clearing old carts task completed")
                );
    }

}
