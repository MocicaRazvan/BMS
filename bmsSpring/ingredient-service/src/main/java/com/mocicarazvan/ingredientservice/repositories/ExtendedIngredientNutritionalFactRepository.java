package com.mocicarazvan.ingredientservice.repositories;

import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.ingredientservice.models.IngredientNutritionalFact;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface ExtendedIngredientNutritionalFactRepository {
    Flux<IngredientNutritionalFact> getModelsFiltered(String name, Boolean display, DietType type, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                      LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageRequest pageRequest);

    Mono<Long> countModelsFiltered(String name, Boolean display, DietType type, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                   LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound);
}
