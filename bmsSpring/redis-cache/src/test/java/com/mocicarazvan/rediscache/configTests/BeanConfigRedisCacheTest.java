package com.mocicarazvan.rediscache.configTests;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.config.BeanConfigRedisCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BeanConfigRedisCache.class)
@Execution(ExecutionMode.SAME_THREAD)
public class BeanConfigRedisCacheTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ReactiveRedisConnectionFactory redisConnectionFactory;
    @Autowired
    ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    @Autowired
    AspectUtils aspectUtils;
    @Autowired
    SimpleAsyncTaskExecutor redisAsyncTaskExecutor;
    @Autowired
    ListableBeanFactory beanFactory;

    @Test
    void expectedBeansAreCreated() {
        assertThat(objectMapper).isNotNull();
        assertThat(redisConnectionFactory).isNotNull();
        assertThat(reactiveRedisTemplate).isNotNull();
        assertThat(aspectUtils).isNotNull();
        assertThat(redisAsyncTaskExecutor).isNotNull();
    }

    @Test
    void expectedBeanNamesAreRegistered() {
        assertThat(beanFactory.containsBean("objectMapper")).isTrue();
        assertThat(beanFactory.containsBean("reactiveRedisConnectionFactory")).isTrue();
        assertThat(beanFactory.containsBean("reactiveRedisTemplate")).isTrue();
        assertThat(beanFactory.containsBean("aspectUtils")).isTrue();
        assertThat(beanFactory.containsBean("redisAsyncTaskExecutor")).isTrue();
    }
}
