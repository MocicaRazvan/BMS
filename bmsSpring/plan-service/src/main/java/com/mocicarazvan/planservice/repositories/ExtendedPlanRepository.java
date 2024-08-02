package com.mocicarazvan.planservice.repositories;

import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.models.Plan;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ExtendedPlanRepository {

    Flux<Plan> getPlansFiltered(String title, Boolean approved, Boolean display, DietType type, PageRequest pageRequest, List<Long> excludeIds);

    Flux<Plan> getPlansFilteredTrainer(String title, Boolean approved, Boolean display, DietType type, Long trainerId, PageRequest pageRequest);

    Mono<Long> countPlansFiltered(String title, Boolean approved, Boolean display, DietType type, List<Long> excludeIds);

    Mono<Long> countPlansFilteredTrainer(String title, Boolean approved, Boolean display, Long trainerId, DietType type);

    Flux<Plan> getPlansFilteredByIds(String title, Boolean approved, Boolean display, DietType type, List<Long> ids, PageRequest pageRequest);

    Mono<Long> countPlansFilteredByIds(String title, Boolean approved, Boolean display, DietType type, List<Long> ids);

}
