package com.mocicarazvan.userservice.email;

import com.mocicarazvan.templatemodule.email.EmailMXCacher;
import com.mocicarazvan.templatemodule.email.config.CustomMailProps;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EmailMXCacherImpl implements EmailMXCacher {
    private final CustomMailProps customMailProps;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Override
    public Mono<Boolean> getCachedMXCheck(String domain) {
        return reactiveRedisTemplate.opsForValue()
                .get(customMailProps.getCacherPrefix() + sanitizeDomain(domain))
                .cast(Boolean.class);
    }

    @Override
    public Mono<Boolean> setCachedMXCheck(String domain, Boolean mxCheck) {
        return reactiveRedisTemplate.opsForValue()
                .set(customMailProps.getCacherPrefix() + sanitizeDomain(domain), mxCheck, customMailProps.getCacherSecondsDuration())
                .cast(Boolean.class);
    }
}
