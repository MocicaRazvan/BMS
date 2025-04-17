package com.mocicarazvan.rediscache.services.impl;

import com.mocicarazvan.rediscache.services.RedisDistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Slf4j
public class RedisDistributedLockImpl implements RedisDistributedLock {
    private final String lockKey;
    private final Long lockTTLSecs;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final AtomicReference<String> lockValue = new AtomicReference<>();


    @Override
    public Mono<Boolean> tryAcquireLock() {
        lockValue.set(UUID.randomUUID().toString());
        return redisTemplate
                .opsForValue().setIfAbsent(
                        lockKey,
                        lockValue.get(),
                        Duration.ofSeconds(lockTTLSecs)
                );
    }

    @Override
    public Mono<Boolean> removeLock() {
        if (lockValue.get() == null) {
            return Mono.just(false);
        }

        return Mono.defer(() -> {

            String script = """
                    if redis.call("GET", KEYS[1]) == ARGV[1] then
                        redis.call("DEL", KEYS[1])
                        return true
                    else
                        return false
                    end
                    """;
            RedisScript<Boolean> redisScript = RedisScript.of(script, Boolean.class);

            return redisTemplate.execute(redisScript,
                            List.of(lockKey),
                            List.of(lockValue.get())
                    )
                    .single()
                    .map(Boolean::booleanValue)
                    .onErrorResume(e -> {
                        log.error("Error while removing lock", e);
                        return Mono.just(false);
                    })
                    .doOnNext(removed -> {
//                        if (removed) {
//                            log.info("Lock {} released by value {}", lockKey, lockValue);
//                        } else {
//                            log.warn("Lock {} NOT released — value mismatch ", lockKey);
//                        }
                    })
                    .doFinally(_ -> {
                        lockValue.set(null);
                    });
        });
    }

}
