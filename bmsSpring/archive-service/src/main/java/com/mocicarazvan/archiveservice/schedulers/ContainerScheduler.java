package com.mocicarazvan.archiveservice.schedulers;


import com.mocicarazvan.archiveservice.config.QueuesPropertiesConfig;
import com.mocicarazvan.archiveservice.services.ContainerNotify;
import com.mocicarazvan.archiveservice.triggers.AfterMillisTrigger;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Slf4j
@Getter
@AllArgsConstructor
public class ContainerScheduler {
    private final SimpleMessageListenerContainer simpleMessageListenerContainer;
    private final SimpleAsyncTaskScheduler simpleAsyncTaskScheduler;
    private final QueuesPropertiesConfig queuesPropertiesConfig;
    private final ContainerNotify containerNotify;
    private AtomicReference<ScheduledFuture<?>> stopTask = new AtomicReference<>();
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

        stopPrevious();
        stopContainerGracefully();
//        if (simpleMessageListenerContainer.isRunning()) {
//            // for * cron expression
////            return;
//        }

        log.info("Starting container for queue: {}", queueName);
        simpleMessageListenerContainer.start();

        containerNotify.notifyContainersStartCron(queueName);

        stopTask.set(simpleAsyncTaskScheduler.schedule(
                this::stopContainerGracefully,
                new AfterMillisTrigger(queuesPropertiesConfig.getSchedulerAliveMillis())
        ));

        log.info("Container will be stopped after {} s", Objects.requireNonNull(stopTask.get()).getDelay(TimeUnit.SECONDS));
    }

    public void startContainerForFixedTime(long aliveMillis) {
        simpleAsyncTaskScheduler.submit(
                () -> startContainerForFixedTimeInternal(aliveMillis)
        );
    }

    private void startContainerForFixedTimeInternal(long aliveMillis) {
        String queueName = simpleMessageListenerContainer.getQueueNames()[0];
        stopPrevious();
        stopContainerGracefully();

        log.info("Manually starting container for queue: {} for {} s", queueName, Duration.ofMillis(aliveMillis).getSeconds());
        simpleMessageListenerContainer.start();

        containerNotify.notifyContainersStartManual(queueName);

        stopTask.set(simpleAsyncTaskScheduler.schedule(
                this::stopContainerGracefully,
                new AfterMillisTrigger(aliveMillis)
        ));


    }

    public void stopContainerManually() {
        simpleAsyncTaskScheduler.submit(
                this::stopContainerManuallyInternal
        );
    }

    private void stopContainerManuallyInternal() {
        String queueName = simpleMessageListenerContainer.getQueueNames()[0];
        log.info("Manually stopping container for queue: {}", queueName);

        stopPrevious();
        stopContainerGracefully();
    }

    private void stopPrevious() {
        ScheduledFuture<?> currentTask = stopTask.get();
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(false);
            log.info("Cancelled previously scheduled stop task for queue");
        }
    }

    private void stopContainerGracefully() {
        if (!simpleMessageListenerContainer.isRunning()) {
            log.info("Container is not running. Skipping stop operation.");
            return;
        }

        String queueName = simpleMessageListenerContainer.getQueueNames()[0];
        log.info("Stopping container for queue: {}", queueName);

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
            containerNotify.notifyContainersStop(queueName);
        } else {
            log.warn("Container did not stop within the expected time: {}", Arrays.toString(simpleMessageListenerContainer.getQueueNames()));
        }
    }

    private boolean waitForContainerToStop() {
        final int[] attempts = {0};
        ScheduledFuture<?> scheduledStop = null;
        try {
            CompletableFuture<Boolean> containerStopped = new CompletableFuture<>();
            scheduledStop = simpleAsyncTaskScheduler.schedule(() -> {
                log.info("Attempt {} to check if container stopped.", attempts[0]);
                if (!simpleMessageListenerContainer.isRunning()) {
                    containerStopped.complete(true);
                } else if (attempts[0]++ >= maxAttempts) {
                    containerStopped.complete(false);
                }
            }, new PeriodicTrigger(Duration.ofMillis(intervalMillis)));

            return containerStopped.get((long) maxAttempts * intervalMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Error while waiting for container to stop.", e);
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (scheduledStop != null && !scheduledStop.isDone()) {
                scheduledStop.cancel(false);
                log.info("Cancelled containerStopped scheduled stop task.");

            }
        }
    }

    @PreDestroy
    public void destroy() {
        stopPrevious();
        stopContainerGracefully();
    }

}

