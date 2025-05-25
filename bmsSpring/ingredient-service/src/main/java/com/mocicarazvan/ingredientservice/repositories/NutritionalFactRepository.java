package com.mocicarazvan.ingredientservice.repositories;

import com.mocicarazvan.ingredientservice.models.NutritionalFact;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NutritionalFactRepository extends ManyToOneUserRepository<NutritionalFact> {

    Mono<NutritionalFact> findByIngredientId(Long ingredientId);

    @Query("""
            SELECT * FROM nutritional_fact
            WHERE created_at >= make_timestamp(:year, :month, 1, 0, 0, 0)
            AND created_at < make_timestamp(:year, :month, 1, 0, 0, 0) + INTERVAL '1 month'
            ORDER BY created_at DESC
            """)
    Flux<NutritionalFact> findModelByMonth(int month, int year);
}
