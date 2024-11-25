package com.mocicarazvan.rediscache.utils;

import org.springframework.data.redis.core.ReactiveRedisTemplate;

//@Component

public class RedisChildCacheUtils extends RedisCacheUtils {

    public RedisChildCacheUtils(AspectUtils aspectUtils, ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        super(aspectUtils, reactiveRedisTemplate);
    }

    // key to not put master name
    public String getMasterKey(String key, Long mId) {
        return key + ":master:" + mId + ":";
    }

}
