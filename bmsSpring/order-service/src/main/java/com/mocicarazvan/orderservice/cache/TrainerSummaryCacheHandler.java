package com.mocicarazvan.orderservice.cache;

import com.mocicarazvan.orderservice.dtos.clients.PlanResponse;
import com.mocicarazvan.orderservice.dtos.summaries.DailyOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.MonthlyOrderSummary;
import com.mocicarazvan.templatemodule.cache.BaseCaffeineCacher;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCache;
import com.mocicarazvan.templatemodule.cache.impl.BaseCaffeienCacherImpl;
import com.mocicarazvan.templatemodule.cache.impl.FilteredListCaffeineCacheBaseImpl;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class TrainerSummaryCacheHandler {

    private final FilteredListCaffeineCache<FilterKeyType, MonthlyOrderSummary> monthlyCache;
    private final FilteredListCaffeineCache<FilterKeyType, DailyOrderSummary> dailyCache;

    public TrainerSummaryCacheHandler() {
        monthlyCache = new FilteredListCaffeineCacheBaseImpl<>("trainer-monthlyOrderSummary");
        dailyCache = new FilteredListCaffeineCacheBaseImpl<>("trainer-dailyOrderSummary");
    }

    private Mono<Void> invalidateCacheForTrainersGeneric(List<PlanResponse> plans, FilteredListCaffeineCache<FilterKeyType, ?> cache) {
//        Set<Long> trainerIds = plans.stream().map(WithUserDto::getUserId).collect(Collectors.toSet());
//        Predicate<FilterKeyType> predicate = k -> false;
//        for (Long trainerId : trainerIds) {
//            predicate = predicate.or(cache.byTrainerIdPredicate(trainerId));
//        }

        Predicate<FilterKeyType> predicate = plans.stream().map(WithUserDto::getUserId).
                reduce(
                        k -> false,
                        (acc, trainerId) -> acc.or(cache.byTrainerIdPredicate(trainerId)),
                        Predicate::or
                );
        return cache.invalidateByVoid(predicate);
    }

    public Mono<Void> invalidateCacheForTrainers(List<PlanResponse> plans) {
        return Mono.when(
                invalidateCacheForTrainersGeneric(plans, monthlyCache),
                invalidateCacheForTrainersGeneric(plans, dailyCache)
        );
    }

    private <T> Flux<T> getTrainerOrdersSummary(Flux<T> flux,
                                                LocalDate from,
                                                LocalDate to,
                                                Long trainerId,
                                                List<PlanResponse> plans,
                                                FilteredListCaffeineCache<FilterKeyType, T> cache,
                                                String uniqueGiver

    ) {
        return cache.getUniqueFluxCacheForTrainer(
                EntitiesUtils.getListOfNotNullObjects(from, to, trainerId, plans),
                trainerId,
                uniqueGiver,
                i -> -11L,
                flux
        );

    }

    public Flux<MonthlyOrderSummary> getTrainersMonthlyOrdersSummary(Flux<MonthlyOrderSummary> flux, LocalDate from, LocalDate to, Long trainerId, List<PlanResponse> plans) {
        return getTrainerOrdersSummary(flux, from, to, trainerId, plans, monthlyCache, "getTrainersMonthlyOrdersSummary");
    }

    public Flux<DailyOrderSummary> getTrainersDailyOrdersSummary(Flux<DailyOrderSummary> flux, LocalDate from, LocalDate to, Long trainerId, List<PlanResponse> plans) {
        return getTrainerOrdersSummary(flux, from, to, trainerId, plans, dailyCache, "getTrainersDailyOrdersSummary");
    }

}
