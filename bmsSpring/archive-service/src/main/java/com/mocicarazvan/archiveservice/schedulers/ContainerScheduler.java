package com.mocicarazvan.archiveservice.schedulers;


import com.mocicarazvan.archiveservice.config.QueuesPropertiesConfig;
import com.mocicarazvan.archiveservice.services.ContainerNotify;
import com.mocicarazvan.archiveservice.triggers.AfterMillisTrigger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Getter
@AllArgsConstructor
public class ContainerScheduler {
    private final SimpleMessageListenerContainer simpleMessageListenerContainer;
    private final SimpleAsyncTaskScheduler simpleAsyncTaskScheduler;
    private final QueuesPropertiesConfig queuesPropertiesConfig;
    private final ContainerNotify containerNotify;
    private ScheduledFuture<?> stopTask;
    private int maxAttempts = 150;
    private int intervalMillis = 500;

    public void scheduleContainer() {
        simpleAsyncTaskScheduler.schedule(
                this::scheduleCron,
                new CronTrigger(queuesPropertiesConfig.getQueueJob(simpleMessageListenerContainer.getQueueNames()[0]))
        );

    }


    private void scheduleCron() {
        String queueName = simpleMessageListenerContainer.getQueueNames()[0];

        stopPrevious(queueName);
        stopContainerGracefully();
//        if (simpleMessageListenerContainer.isRunning()) {
//            // for * cron expression
////            return;
//        }

        log.info("Starting container for queue: {}", queueName);
        simpleMessageListenerContainer.start();

        containerNotify.notifyContainersStartCron(queueName);

        stopTask = simpleAsyncTaskScheduler.schedule(
                this::stopContainerGracefully,
                new AfterMillisTrigger(queuesPropertiesConfig.getSchedulerAliveMillis())
        );

        log.info("Container will be stopped after {} s", Objects.requireNonNull(stopTask).getDelay(TimeUnit.SECONDS));
    }

    public void startContainerForFixedTime(long aliveMillis) {
        String queueName = simpleMessageListenerContainer.getQueueNames()[0];
        stopPrevious(queueName);
        stopContainerGracefully();

        log.info("Manually starting container for queue: {} for {} s", queueName, Duration.ofMillis(aliveMillis).getSeconds());
        simpleMessageListenerContainer.start();

        containerNotify.notifyContainersStartManual(queueName);

        stopTask = simpleAsyncTaskScheduler.schedule(
                this::stopContainerGracefully,
                new AfterMillisTrigger(aliveMillis)
        );
    }

    public void stopContainerManually() {
        String queueName = simpleMessageListenerContainer.getQueueNames()[0];
        log.info("Manually stopping container for queue: {}", queueName);

        stopPrevious(queueName);
        stopContainerGracefully();
    }

    private void stopPrevious(String queueName) {
        if (stopTask != null && !stopTask.isDone()) {
            stopTask.cancel(false);
            log.info("Cancelled previously scheduled stop task for queue: {}", queueName);
        }
    }

    private void stopContainerGracefully() {
        if (!simpleMessageListenerContainer.isRunning()) {
            log.info("Container is not running. Skipping stop operation.");
            return;
        }

        String queueName = simpleMessageListenerContainer.getQueueNames()[0];
        log.info("Stopping container gracefully for queue: {}", queueName);

        try {
            simpleMessageListenerContainer.stop();

            boolean stopped = waitForContainerToStop();
            if (stopped) {
                log.info("Container stopped gracefully for queue: {}", queueName);
            } else {
                log.warn("Container did not stop gracefully within the timeout. Forcing shutdown for queue: {}", queueName);
                simpleMessageListenerContainer.shutdown();
            }
        } catch (Exception e) {
            log.error("Error while stopping container for queue: {}", queueName, e);
            Thread.currentThread().interrupt();
        }
        if (!simpleMessageListenerContainer.isRunning()) {
            log.info("Container stopped gracefully: {}", Arrays.toString(simpleMessageListenerContainer.getQueueNames()));
            containerNotify.notifyContainersStop(queueName);
        } else {
            log.warn("Container did not stop within the expected time: {}", Arrays.toString(simpleMessageListenerContainer.getQueueNames()));
        }
    }

    private boolean waitForContainerToStop() {
        for (int i = 0; i < maxAttempts; i++) {
            if (!simpleMessageListenerContainer.isRunning()) {
                return true;
            }
            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting for container to stop.");
                return false;
            }
        }
        return false;
    }


}

