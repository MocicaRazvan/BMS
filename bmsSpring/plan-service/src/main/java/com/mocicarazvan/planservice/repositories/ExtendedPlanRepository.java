package com.mocicarazvan.planservice.repositories;

import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.enums.ObjectiveType;
import com.mocicarazvan.planservice.models.Plan;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface ExtendedPlanRepository {

    Flux<Plan> getPlansFiltered(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, PageRequest pageRequest, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound);

    Flux<Plan> getPlansFilteredTrainer(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, Long trainerId, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                       LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageRequest pageRequest);

    Mono<Long> countPlansFiltered(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                  LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound);

    Mono<Long> countPlansFilteredTrainer(String title, Boolean approved, Boolean display, Long trainerId, DietType type, ObjectiveType objective, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                         LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound);

    Flux<Plan> getPlansFilteredByIds(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> ids, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                     LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageRequest pageRequest);

    Mono<Long> countPlansFilteredByIds(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> ids, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                       LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound);

}
