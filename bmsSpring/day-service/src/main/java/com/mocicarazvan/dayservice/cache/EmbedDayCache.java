package com.mocicarazvan.dayservice.cache;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.ollamasearch.cache.EmbedCache;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class EmbedDayCache implements EmbedCache {
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final ObjectMapper objectMapper;
    private final String EMBEDDING_KEY = "dayEmbedding:";
    @Value("${spring.custom.ollama.cache.expire.minutes:5}")
    private Long expireMinutes;

    @Override
    public Mono<OllamaEmbedResponseModel> getEmbedding(String text, Function<String, OllamaEmbedResponseModel> cacheMissFunction) {
        String key = EMBEDDING_KEY + text.trim().toLowerCase();
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .map(embedding -> objectMapper.convertValue(embedding, OllamaEmbedResponseModel.class))
                .switchIfEmpty(Mono.defer(() -> {
                    OllamaEmbedResponseModel embedding = cacheMissFunction.apply(text);
                    return reactiveRedisTemplate.opsForValue()
                            .set(key, embedding, Duration.ofMinutes(expireMinutes))
                            .thenReturn(embedding);
                }));
    }
}
