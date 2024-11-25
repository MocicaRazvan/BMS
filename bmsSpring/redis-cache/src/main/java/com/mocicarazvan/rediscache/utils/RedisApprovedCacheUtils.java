package com.mocicarazvan.rediscache.utils;


import com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCache;
import com.mocicarazvan.rediscache.enums.BooleanEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

//@Component
public class RedisApprovedCacheUtils extends RedisCacheUtils {

    public RedisApprovedCacheUtils(AspectUtils aspectUtils, ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        super(aspectUtils, reactiveRedisTemplate);
    }

    public BooleanEnum getApprovedArg(ProceedingJoinPoint joinPoint, RedisReactiveApprovedCache annotation) {
        if (annotation.approvedArgumentPath() == null || annotation.approvedArgumentPath().isBlank()) {
            return annotation.approved();
        }
        return BooleanEnum.fromObject(aspectUtils.evaluateSpelExpression(annotation.approvedArgumentPath(), joinPoint));
    }

    public String getApprovedKey(BooleanEnum app) {
        return ":" + "approved:" + app.getValue();
    }

    public String getForWhomKey(Long forWhomId) {
        return ":" + "forWhom:" + forWhomId;
    }
}
