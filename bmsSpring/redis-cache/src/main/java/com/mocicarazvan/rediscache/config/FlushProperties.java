package com.mocicarazvan.rediscache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.custom.flush")
@Data
public class FlushProperties {
    private Long timeout = 15L;

    private int parallelism = 5;

    private boolean enabled = true;

    public void setTimeout(Long timeout) {
        if (timeout < 2L) {
            throw new IllegalArgumentException("Timeout must be greater than 2");
        }
        this.timeout = timeout;
    }

    public void setParallelism(int parallelism) {
        if (parallelism < 1) {
            throw new IllegalArgumentException("Parallelism must be greater than 0");
        }
        this.parallelism = parallelism;
    }
}
