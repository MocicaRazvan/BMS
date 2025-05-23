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

import java.util.Collection;
import java.util.List;

public interface RecipeRepository extends ApprovedRepository<Recipe>, CountInParent, CountIds {
    Flux<Recipe> findAllByTitleContainingIgnoreCaseAndApprovedAndType(String title, boolean approved, DietType type, PageRequest pageRequest);

    Mono<Long> countAllByTitleContainingIgnoreCaseAndApprovedAndType(String title, boolean approved, DietType type);


    @Query("""
                select count(r.id) from recipe r
                join ingredient_quantity i on r.id = i.recipe_id
                where i.ingredient_id = :childId
            """)
    Mono<Long> countInParent(Long childId);

    @Override
    @Query(""" 
                            select count(r.id) from recipe r 
                            where r.approved = true and r.id in (:ids)
            """)
    Mono<Long> countByIds(Collection<Long> ids);


    @Query("""
                select count(*) from recipe r
                where r.approved = true and r.user_id = :userId and r.id in (:ids)
            """)
    Mono<Long> countAllByIdsUser(List<Long> ids, Long userId);

    Flux<Recipe> findAllByIdInAndApprovedTrue(List<Long> ids);

    @Query("""
            SELECT * FROM recipe
            WHERE created_at >= make_timestamp(:year, :month, 1, 0, 0, 0)
            AND created_at < make_timestamp(:year, :month, 1, 0, 0, 0) + INTERVAL '1 month'
            ORDER BY created_at DESC
            """)
    Flux<Recipe> findModelByMonth(int month, int year);
}
