package com.mocicarazvan.recipeservice.repositories;

import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.recipeservice.models.Recipe;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface RecipeExtendedRepository {
    Flux<Recipe> getRecipesFiltered(String title, Boolean approved, DietType type, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                    LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageRequest pageRequest);

    Flux<Recipe> getRecipesFilteredTrainer(String title, Boolean approved, DietType type, Long trainerId, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                           LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageRequest pageRequest);

    Mono<Long> countRecipesFiltered(String title, Boolean approved, DietType type, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                    LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound);

    Mono<Long> countRecipesFilteredTrainer(String title, Boolean approved, Long trainerId, DietType type, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                           LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound);

    Mono<DietType> determineMostRestrictiveDietType(List<Long> recipeIds);
}
