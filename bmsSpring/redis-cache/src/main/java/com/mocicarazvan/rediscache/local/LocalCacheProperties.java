package com.mocicarazvan.rediscache.local;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.custom.cache.redis.local")
public class LocalCacheProperties {
    private Long expireMinutes = 15L;
    private int maxSize = 1000;
}
