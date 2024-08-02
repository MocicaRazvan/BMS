package com.mocicarazvan.recipeservice.repositories;

import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.recipeservice.models.Recipe;
import com.mocicarazvan.templatemodule.repositories.ApprovedRepository;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.CountInParent;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RecipeRepository extends ApprovedRepository<Recipe>, CountInParent, CountIds {
    Flux<Recipe> findAllByTitleContainingIgnoreCaseAndApprovedAndType(String title, boolean approved, DietType type, PageRequest pageRequest);

    Mono<Long> countAllByTitleContainingIgnoreCaseAndApprovedAndType(String title, boolean approved, DietType type);


    @Query("""
                select count(*) from recipe r
                join ingredient_quantity i on r.id = i.recipe_id
                where i.ingredient_id = :childId
            """)
    Mono<Long> countInParent(Long childId);

    @Query(""" 
                            select count(*) from recipe r 
                            where r.id in (:ids) and r.approved = true
            """)
    Mono<Long> countByIds(List<Long> ids);


    @Query("""
                select count(*) from recipe r
                where r.id in (:ids) and r.approved = true and r.user_id = :userId
            """)
    Mono<Long> countAllByIdsUser(List<Long> ids, Long userId);

    Flux<Recipe> findAllByIdInAndApprovedTrue(List<Long> ids);

    @Query("""
            SELECT * FROM recipe
            WHERE EXTRACT(MONTH FROM created_at) = :month
            AND EXTRACT(YEAR FROM created_at) = :year
            ORDER BY created_at DESC
            """)
    Flux<Recipe> findModelByMonth(int month, int year);
}
