package com.mocicarazvan.ingredientservice.repositories;

import com.mocicarazvan.ingredientservice.models.NutritionalFact;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NutritionalFactRepository extends ManyToOneUserRepository<NutritionalFact> {

    Mono<NutritionalFact> findByIngredientId(Long ingredientId);

    @Query("""
            SELECT * FROM nutritional_fact
            WHERE EXTRACT(MONTH FROM created_at) = :month
            AND EXTRACT(YEAR FROM created_at) = :year
            ORDER BY created_at DESC
            """)
    Flux<NutritionalFact> findModelByMonth(int month, int year);
}
