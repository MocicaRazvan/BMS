package com.mocicarazvan.cartservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.support.CronExpression;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class CronUtils {


    public static Pair<Duration, Duration> computeDurations(String cron, LocalDateTime currentTime) {
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
