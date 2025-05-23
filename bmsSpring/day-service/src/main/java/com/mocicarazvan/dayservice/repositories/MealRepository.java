package com.mocicarazvan.dayservice.repositories;

import com.mocicarazvan.dayservice.models.Meal;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MealRepository extends ManyToOneUserRepository<Meal> {
    Flux<Meal> findAllByDayId(Long dayId, Sort sort);

    @Query("""
            SELECT * FROM meal
            WHERE created_at >= make_timestamp(:year, :month, 1, 0, 0, 0)
            AND created_at < make_timestamp(:year, :month, 1, 0, 0, 0) + INTERVAL '1 month'
            ORDER BY created_at DESC
            """)
    Flux<Meal> findModelByMonth(int month, int year);


    Mono<Void> deleteAllByDayId(Long dayId);

    @Query("""
             SELECT EXISTS(
                 SELECT 1
                 FROM meal m
                 join meal_recipes mr on m.id = mr.master_id
                 WHERE day_id = :dayId
                 AND mr.child_id= :recipeId
             )
            """)
    Mono<Boolean> existsByDayIdAndRecipesContaining(Long dayId, Long recipeId);

    @Query("""
            SELECT DISTINCT UNNEST(recipes)
            FROM meal
            WHERE day_id = ANY (:dayIds)
            """)
    Flux<Long> findUniqueRecipeIdsByDayIds(Long[] dayIds);

    @Query("""
            SELECT * FROM meal 
            WHERE day_id = :dayId 
            ORDER BY 
                CAST(SPLIT_PART(period, ':', 1) AS int), 
                CAST(SPLIT_PART(period, ':', 2) AS int)
            """)
    Flux<Meal> findAllByDayIdCustomPeriodSort(Long dayId);

    Flux<Meal> findAllByDayId(Long dayId);
}
