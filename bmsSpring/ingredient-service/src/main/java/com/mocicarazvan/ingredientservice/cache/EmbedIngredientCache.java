package com.mocicarazvan.ingredientservice.cache;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.ollamasearch.cache.EmbedCache;
import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedResponseModel;
import com.mocicarazvan.rediscache.services.impl.SaveObjectToCacheImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component

public class EmbedIngredientCache extends SaveObjectToCacheImpl implements EmbedCache {

    @Value("${spring.custom.ollama.cache.expire.minutes:5}")
    private Long expireMinutes;


    public EmbedIngredientCache(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate, ObjectMapper objectMapper) {
        super(reactiveRedisTemplate, objectMapper);
    }

    @Override
    public Mono<OllamaEmbedResponseModel> getEmbedding(String text, Function<String, Mono<OllamaEmbedResponseModel>> cacheMissFunction) {
        return getOrSaveObject(text, expireMinutes, cacheMissFunction, (t) -> getEmbeddingKey("ingredientEmbedding", t), new TypeReference<>() {
        });
    }
}
