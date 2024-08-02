package com.mocicarazvan.ingredientservice.repositories;

import com.mocicarazvan.ingredientservice.models.Ingredient;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomIngredientRepository {
    Flux<Ingredient> findAllByExample(Ingredient example, Pageable pageable);

    Mono<Long> countByExample(Ingredient example);
}