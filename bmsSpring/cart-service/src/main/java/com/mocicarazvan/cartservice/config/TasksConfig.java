package com.mocicarazvan.cartservice.config;

import com.mocicarazvan.cartservice.services.UserCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.support.CronExpression;
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
            return null;
        }

        Pair<Duration, Duration> durations = computeDurations(clearOldCartsCron, LocalDateTime.now());
        return Flux.interval(durations.getFirst(), durations.getSecond())
                .concatMap(_ -> {
                    log.info("Clearing old carts");
                    return
                            userCartService.countAll()
                                    .doOnNext(count -> log.info("Old carts count before delete: {}", count))
                                    .then(userCartService.deleteOldCars(LocalDateTime.now().minusDays(clearOldCartsDaysCutoff)))
                                    .then(userCartService.countAll()
                                            .doOnNext(count -> log.info("Old carts count after delete: {}", count))
                                    );
                })
                .subscribe();
    }


    public Pair<Duration, Duration> computeDurations(String cron, LocalDateTime currentTime) {
        CronExpression cronExpression = CronExpression.parse(cron);

        LocalDateTime initExecutionTime = cronExpression.next(currentTime);
        if (initExecutionTime == null) {
            initExecutionTime = LocalDateTime.now();
        }
        LocalDateTime nextExecutionTime = cronExpression.next(initExecutionTime);

        log.info("Init execution time: {}", initExecutionTime);
        log.info("Next execution time: {}", nextExecutionTime);

        Duration initDelay = Duration.between(currentTime, initExecutionTime);
        Duration interval = Duration.between(initExecutionTime, nextExecutionTime);

        return Pair.of(initDelay, interval);
    }
}
