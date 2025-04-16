package com.mocicarazvan.fileservice.utils;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.support.CronExpression;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CronUtilsTest {

    @Test
    void computeDurationsReturnsCorrectDurationsForValidCron() {
        LocalDateTime currentTime = LocalDateTime.of(2025, 5, 7, 12, 0, 0);
        LocalDateTime initExecutionTime = currentTime.plusSeconds(10);
        LocalDateTime nextExecutionTime = initExecutionTime.plusSeconds(30);
        CronExpression mockCronExpression = Mockito.mock(CronExpression.class);
        Mockito.when(mockCronExpression.next(currentTime)).thenReturn(initExecutionTime);
        Mockito.when(mockCronExpression.next(initExecutionTime)).thenReturn(nextExecutionTime);
        try (MockedStatic<CronExpression> cronStatic = Mockito.mockStatic(CronExpression.class)) {
            cronStatic.when(() -> CronExpression.parse(ArgumentMatchers.anyString()))
                    .thenReturn(mockCronExpression);
            Pair<Duration, Duration> durations = CronUtils.computeDurations("dummyCron", currentTime);
            assertEquals(Duration.between(currentTime, initExecutionTime), durations.getFirst());
            assertEquals(Duration.between(initExecutionTime, nextExecutionTime), durations.getSecond());
        }
    }

}