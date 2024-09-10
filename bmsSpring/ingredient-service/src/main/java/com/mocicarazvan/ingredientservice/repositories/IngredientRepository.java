package com.mocicarazvan.ingredientservice.repositories;

import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.ingredientservice.models.Ingredient;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IngredientRepository extends ManyToOneUserRepository<Ingredient>, CountIds {


    Flux<Ingredient> findAllByNameContainingIgnoreCaseAndDisplayAndType(String name, boolean display, DietType type, Pageable pageable);

    Mono<Long> countAllByNameContainingIgnoreCaseAndDisplayAndType(String name, boolean display, DietType type);

    Flux<Ingredient> findAllByNameContainingIgnoreCaseAndDisplay(String name, boolean display, Pageable pageable);

    Mono<Long> countAllByNameContainingIgnoreCaseAndDisplay(String name, boolean display);

    Flux<Ingredient> findAllByNameContainingIgnoreCaseAndType(String name, DietType type, Pageable pageable);

    Mono<Long> countAllByNameContainingIgnoreCaseAndType(String name, DietType type);

    Flux<Ingredient> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    Mono<Long> countAllByNameContainingIgnoreCase(String name);

    Mono<Boolean> existsByNameIgnoreCase(String name);

    Mono<Boolean> existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Override
    @Query("""
                select distinct i.id from  ingredient i
                where i.id in (:ids) and i.display = true
            """)
    Flux<Long> countByIds(List<Long> ids);

    Flux<Ingredient> findAllByIdInAndDisplayTrue(List<Long> ids);

    @Query("""
            SELECT * FROM ingredient
            WHERE EXTRACT(MONTH FROM created_at) = :month
            AND EXTRACT(YEAR FROM created_at) = :year
            ORDER BY created_at DESC
            """)
    Flux<Ingredient> findModelByMonth(int month, int year);

}
