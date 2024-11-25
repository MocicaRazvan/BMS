package com.mocicarazvan.userservice.cache.redis.utils;

import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisCacheUtils;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.userservice.cache.redis.enums.RoleAnn;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRoleCacheUtils extends RedisCacheUtils {
    public RedisRoleCacheUtils(AspectUtils aspectUtils, ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        super(aspectUtils, reactiveRedisTemplate);
    }

    public String getRoleKey(RoleAnn roleAnn) {
        return STR.":role:\{roleAnn.getRole()}";
    }

    public Role assertRole(Object role) {
        if (role == null) {
            return null;
        }
        if (!(role instanceof Role)) {
            throw new IllegalArgumentException("Invalid value for Role: " + role);
        }
        return (Role) role;
    }
}
