package com.mocicarazvan.archiveservice.config;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "spring.custom.queues")
@Data
@NoArgsConstructor
public class QueuesPropertiesConfig {
    private Map<String, String> queuesJobs;
    private int batchSize = 1000;
    private String concurrency = "1-3";
    private int schedulerAliveMillis = 1200000;
    private int schedulerRepeatSeconds = 60;
    private int savingBufferSeconds = 60;

    public List<String> getQueues() {
        return List.copyOf(queuesJobs.keySet());
    }

    public String getQueueJob(String queue) {
        return queuesJobs.get(queue);
    }


}
