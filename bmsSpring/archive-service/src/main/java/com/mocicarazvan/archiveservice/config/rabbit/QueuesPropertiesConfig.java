package com.mocicarazvan.archiveservice.config.rabbit;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "spring.custom.queues")
@Data
@NoArgsConstructor
public class QueuesPropertiesConfig {
    private Map<String, String> queuesJobs;
    private int batchSize = 1000;
    private String concurrency = "1-5";
    private int schedulerAliveMillis = 1200000;
    private int schedulerRepeatSeconds = 60;
    private int savingBufferSeconds = 60;

    public Set<String> getQueues() {
        return queuesJobs.keySet();
    }

    public String getQueueJob(String queue) {
        return queuesJobs.get(queue);
    }

    public void setQueuesJobs(Map<String, String> queuesJobs) {
        if (queuesJobs == null || queuesJobs.isEmpty()) {
            throw new IllegalArgumentException("Queues jobs map cannot be null or empty.");
        }
        queuesJobs.values().forEach(v -> {
            if (v == null || v.isBlank()) {
                throw new IllegalArgumentException("Cron expression cannot be null or empty.");
            }
            CronExpression.parse(v);
            if ("* * * * * *".equals(v.strip())) {
                throw new IllegalArgumentException("Cron expression cannot be set to '* * * * * *' as it runs always.");
            }
        });
        this.queuesJobs = queuesJobs;
    }
    
}
