package com.mocicarazvan.archiveservice.containers;


import com.mocicarazvan.archiveservice.config.QueuesPropertiesConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Getter
public class ContainerScheduler {
    private final SimpleMessageListenerContainer simpleMessageListenerContainer;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final QueuesPropertiesConfig queuesPropertiesConfig;

    public void scheduleContainer() {
        String queueName = simpleMessageListenerContainer.getQueueNames()[0];
        CronTrigger cronTrigger = new CronTrigger(queuesPropertiesConfig.getQueueJob(queueName));

        threadPoolTaskScheduler.schedule(
                this::handleContainerLifecycle,
                cronTrigger
        );

    }


    private void handleContainerLifecycle() {
        String queueName = simpleMessageListenerContainer.getQueueNames()[0];
        try {
            log.info("Starting container for queue: " + queueName);
            simpleMessageListenerContainer.start();


            Thread.sleep(queuesPropertiesConfig.getSchedulerAliveMillis());

            log.info("Stopping container for queue: " + queueName);
            stopContainerGracefully();
        } catch (InterruptedException e) {
            log.error("Error handling container lifecycle: " + e.getMessage());
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private void stopContainerGracefully() {
        simpleMessageListenerContainer.stop();
        int maxWaitTimeMillis = 100000;
        int waitIntervalMillis = 200;
        int waitedTime = 0;

        while (simpleMessageListenerContainer.isRunning() && waitedTime < maxWaitTimeMillis) {
            try {
                Thread.sleep(waitIntervalMillis);
                waitedTime += waitIntervalMillis;
            } catch (InterruptedException e) {
                log.error("Interrupted while stopping container for queue: " + Arrays.toString(simpleMessageListenerContainer.getQueueNames()), e);
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (simpleMessageListenerContainer.isRunning()) {
            log.warn("Container did not stop gracefully within the timeout. Forcing stop: " + Arrays.toString(simpleMessageListenerContainer.getQueueNames()));

            simpleMessageListenerContainer.shutdown();
            try {

                Thread.sleep(TimeUnit.SECONDS.toMillis(25));
            } catch (InterruptedException e) {
                log.error("Interrupted while stopping container for queue: " + Arrays.toString(simpleMessageListenerContainer.getQueueNames()), e);
                Thread.currentThread().interrupt();
            }
        }

        if (!simpleMessageListenerContainer.isRunning()) {
            log.info("Container stopped gracefully: " + Arrays.toString(simpleMessageListenerContainer.getQueueNames()));
        } else {

            log.warn("Container did not stop within the expected time: " + Arrays.toString(simpleMessageListenerContainer.getQueueNames()));
        }
    }


}

