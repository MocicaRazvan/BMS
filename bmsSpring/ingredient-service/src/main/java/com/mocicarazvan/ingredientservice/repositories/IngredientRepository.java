package com.mocicarazvan.ingredientservice.repositories;

import com.mocicarazvan.ingredientservice.models.Ingredient;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

public interface IngredientRepository extends ManyToOneUserRepository<Ingredient>, CountIds {


    Mono<Boolean> existsByNameIgnoreCase(String name);

    Mono<Boolean> existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Override
    @Query("""
                select count(i.id) from  ingredient i
                where i.id in (:ids) and i.display = true
            """)
    Mono<Long> countByIds(Collection<Long> ids);

    Flux<Ingredient> findAllByIdInAndDisplayTrue(List<Long> ids);

    @Query("""
            SELECT * FROM ingredient
            WHERE created_at >= make_timestamp(:year, :month, 1, 0, 0, 0)
            AND created_at < make_timestamp(:year, :month, 1, 0, 0, 0) + INTERVAL '1 month'
            ORDER BY created_at DESC
            """)
    Flux<Ingredient> findModelByMonth(int month, int year);

}
