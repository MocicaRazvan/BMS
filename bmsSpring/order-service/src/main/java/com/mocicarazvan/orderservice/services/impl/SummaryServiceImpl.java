package com.mocicarazvan.orderservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.orderservice.clients.PlanClient;
import com.mocicarazvan.orderservice.clients.TimeSeriesClient;
import com.mocicarazvan.orderservice.dtos.OrderDto;
import com.mocicarazvan.orderservice.dtos.clients.PlanResponse;
import com.mocicarazvan.orderservice.dtos.summaries.*;
import com.mocicarazvan.orderservice.enums.CountrySummaryType;
import com.mocicarazvan.orderservice.mappers.OrderMapper;
import com.mocicarazvan.orderservice.repositories.SummaryRepository;
import com.mocicarazvan.orderservice.services.SummaryService;
import com.mocicarazvan.orderservice.utils.DateUtils;
import com.mocicarazvan.orderservice.utils.OrderCacheKeys;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@Getter
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {
    private final String modelName = OrderCacheKeys.ORDER_NAME;
    private final UserClient userClient;
    private final SummaryServiceRedisCacheWrapper self;
    private final PlanClient planClient;
    private final SummaryRepository summaryRepository;
    private final TimeSeriesClient timeSeriesClient;
    @Value("${prediction.start.years:3}")
    private int predictionStartYears;


    @Override
    public Flux<MonthlyEntityGroup<OrderDto>> getOrdersGroupedByMonth(int month, String userId) {

        return

                userClient.getUser("", userId)
                        .flatMapMany(userDto ->
                                self.getOrderGroupByMonthBase(month, userDto)
                        );
    }

    @Override
    public Flux<MonthlyOrderSummary> getOrdersSummaryByMonth(LocalDate from, LocalDate to, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getIntervalDates(from, to);

        return
                self.getOrdersSummaryByMonthBase(intervalDates.getFirst(), intervalDates.getSecond());

    }

    @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "2*month+year+66005")
    @Override
    public Flux<MonthlyOrderSummaryPrediction> getOrderSummaryPrediction(int predictionLength) {
        return getSummaryPrediction(summaryRepository::getOrdersSummaryByDateRangeGroupedByMonth, predictionLength);

    }

    @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "2*month+year+71006")
    @Override
    public Flux<MonthlyOrderSummaryPrediction> getPlanSummaryPrediction(int predictionLength) {
        return getSummaryPrediction(summaryRepository::getAdminOrdersPlanSummaryByDateRangeGroupedByMonth, predictionLength);

    }

    @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "2*month+year+76007", masterId = "#trainerId")
    @Override
    public Flux<MonthlyOrderSummaryPrediction> getTrainerPlanSummaryPrediction(int predictionLength, Long trainerId, String userId) {
        return getTrainerSummary(trainerId, userId,
                () -> getSummaryPrediction((f, e) -> summaryRepository.getTrainerOrdersSummaryByDateRangeGroupedByMonth(f, e, trainerId),
                        predictionLength));

    }

    private Flux<MonthlyOrderSummaryPrediction> getSummaryPrediction(BiFunction<LocalDateTime, LocalDateTime, Flux<MonthlyOrderSummary>> repositoryFunction,
                                                                     int predictionLength) {
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime startPrediction = current.minusYears(predictionStartYears).withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);

        return timeSeriesClient.getCountAmountPredictions(repositoryFunction.apply(startPrediction, current), predictionLength)
                .flatMapMany(r -> Flux.range(0, predictionLength)
                        .map(i -> {
                                    LocalDateTime curIns = current.plusMonths(i + 1);
                                    return MonthlyOrderSummaryPrediction.builder()
                                            .countQuantiles(r.getCount_quantiles().get(i))
                                            .totalAmountQuantiles(r.getTotal_amount_quantiles().get(i))
                                            .year(curIns.getYear())
                                            .month(curIns.getMonthValue())
                                            .build();
                                }
                        )
                );

    }

    @Override
    public Flux<MonthlyOrderSummary> getOrdersPlanSummaryByMonth(LocalDate from, LocalDate to, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getIntervalDates(from, to);

        return
                self.getOrdersPlanSummaryByMonthBase(intervalDates.getFirst(), intervalDates.getSecond());

    }

    @Override
    public Flux<TopUsersSummary> getTopUsersSummary(LocalDate from, LocalDate to, int top) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getIntervalDates(from, to);
        return self.getTopUsersSummaryBase(intervalDates.getFirst(), intervalDates.getSecond(), top);
    }

    @Override
    public Flux<TopTrainersSummaryResponse> getTopTrainersSummary(LocalDate from, LocalDate to, int top) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getIntervalDates(from, to);
        return self.getTopTrainersSummaryBase(intervalDates.getFirst(), intervalDates.getSecond(), top);
    }

    @Override
    public Flux<TopPlansSummary> getTopPlansSummary(LocalDate from, LocalDate to, int top) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getIntervalDates(from, to);
        return self.getTopPlansSummaryBase(intervalDates.getFirst(), intervalDates.getSecond(), top);
    }

    @Override
    public Flux<TopPlansSummary> getTopPlansSummaryTrainer(LocalDate from, LocalDate to, int top, Long trainerId, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getIntervalDates(from, to);
        return getTrainerSummaryWrapperPlans(trainerId, userId,
                plans -> self.getTopPlansSummaryTrainerBase(intervalDates.getFirst(), intervalDates.getSecond(), top,
                        plans, trainerId));
    }

    @Override
    public Flux<MonthlyOrderSummary> getTrainerOrdersSummaryByMonth(LocalDate from, LocalDate to, Long trainerId, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getIntervalDates(from, to);

        return getTrainerSummary(trainerId, userId,
                () -> self.getTrainerOrdersSummaryByMonthBase(intervalDates, trainerId));

    }


    @Override
    public Flux<DailyOrderSummary> getOrdersSummaryByDay(LocalDate from, LocalDate to, String userId) {

        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getIntervalDates(from, to);

        return
                self.getOrdersSummaryByDay(intervalDates.getFirst(), intervalDates.getSecond());
    }


    @Override
    public Flux<DailyOrderSummary> getOrdersPlanSummaryByDay(LocalDate from, LocalDate to, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getIntervalDates(from, to);

        return
                self.getOrdersPlanSummaryByDay(intervalDates.getFirst(), intervalDates.getSecond());
    }

    @Override
    public Flux<DailyOrderSummary> getTrainerOrdersSummaryByDay(LocalDate from, LocalDate to, Long trainerId, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getIntervalDates(from, to);

        return getTrainerSummary(trainerId, userId,
                () -> self.getTrainerOrdersSummaryByDayBase(intervalDates, trainerId));

    }


    private <T> Flux<T> getTrainerSummary(Long trainerId, String userId,
                                          Supplier<Flux<T>> callback
    ) {
        return userClient.existsTrainerOrAdmin("/exists", trainerId)
                .thenMany(callback.get());
    }

    private <T> Flux<T> getTrainerSummaryWrapperPlans(Long trainerId, String userId,
                                                      Function<Flux<PlanResponse>, Flux<T>> summaryFunction
    ) {
        return userClient.existsTrainerOrAdmin("/exists", trainerId)
                .thenMany(planClient.getTrainersPlans(String.valueOf(trainerId), userId)
                        .as(summaryFunction)
                );
    }


    @Override
    public Flux<CountryOrderSummary> getOrdersSummaryByCountry(CountrySummaryType type, LocalDate from, LocalDate to) {
        return
                self.getOrdersSummaryByCountryBase(type, from, to);

    }


    @Override
    public Flux<MonthlyOrderSummaryObjective> getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectives(LocalDate month, Long trainerId, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getMonthRange(month);
        return getTrainerSummary(trainerId, userId, () ->
                self.getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectivesBase(intervalDates, trainerId));
    }


    @Override
    public Flux<MonthlyOrderSummaryType> getTrainerOrdersSummaryByDateRangeGroupedByMonthTypes(LocalDate month, Long trainerId, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getMonthRange(month);
        return getTrainerSummary(trainerId, userId, () ->
                self.getTrainerOrdersSummaryByDateRangeGroupedByMonthTypesBase(intervalDates, trainerId));
    }

    @Override
    public Flux<MonthlyOrderSummaryObjectiveType> getTrainOrdersSummaryByDateRangeGroupedByMonthObjectiveTypes(LocalDate month, Long trainerId, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getMonthRange(month);
        return getTrainerSummary(trainerId, userId, () ->
                self.getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectivesTypesBase(intervalDates, trainerId));
    }

    @Override
    public Flux<MonthlyOrderSummaryObjective> getAdminOrdersSummaryByDateRangeGroupedByMonthObjectives(LocalDate month, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getMonthRange(month);
        return
                self.getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectivesBase(intervalDates, -1L);
    }

    @Override
    public Flux<MonthlyOrderSummaryType> getAdminOrdersSummaryByDateRangeGroupedByMonthTypes(LocalDate month, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getMonthRange(month);
        return
                self.getTrainerOrdersSummaryByDateRangeGroupedByMonthTypesBase(intervalDates, -1L);
    }

    @Override
    public Flux<MonthlyOrderSummaryObjectiveType> getAdminOrdersSummaryByDateRangeGroupedByMonthObjectiveTypes(LocalDate month, String userId) {
        Pair<LocalDateTime, LocalDateTime> intervalDates = DateUtils.getMonthRange(month);
        return
                self.getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectivesTypesBase(intervalDates, -1L);
    }

    @Component
    @Getter
    @RequiredArgsConstructor
    public static class SummaryServiceRedisCacheWrapper {
        private final String modelName = OrderCacheKeys.ORDER_NAME;
        private final SummaryRepository summaryRepository;
        private final ObjectMapper objectMapper;
        private final OrderMapper orderMapper;

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "entity.id")
        public Flux<MonthlyEntityGroup<OrderDto>> getOrderGroupByMonthBase(int month, UserDto userDto) {
            if (!userDto.getRole().equals(Role.ROLE_ADMIN)) {
                return Flux.error(new PrivateRouteException());
            }
            LocalDateTime now = LocalDateTime.now();
            int year = now.getYear();
            if (month == 1 && now.getMonthValue() == 1) {
                year -= 1;
            }
            return summaryRepository.findModelByMonth(month, year)
                    .map(m -> {
                                YearMonth ym = YearMonth.from(m.getCreatedAt());
                                return MonthlyEntityGroup.<OrderDto>builder()
                                        .month(ym.getMonthValue())
                                        .year(ym.getYear())
                                        .entity(orderMapper.fromModelToDto(m))
                                        .build();
                            }
                    );
        }

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "userId+rank*2+2501")
        public Flux<TopUsersSummary> getTopUsersSummaryBase(LocalDateTime from, LocalDateTime to, int top) {
            return summaryRepository.getTopUsersSummary(from, to, top);
        }

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "userId+rank*5+8502")
        public Flux<TopPlansSummary> getTopPlansSummaryBase(LocalDateTime from, LocalDateTime to, int top) {
            return summaryRepository.getTopPlansSummary(from, to, top);
        }

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "userId+rank*7+12003", masterId = "#trainerId")
        public Flux<TopPlansSummary> getTopPlansSummaryTrainerBase(LocalDateTime from, LocalDateTime to, int top, Flux<PlanResponse> plans, Long trainerId) {
            return getPlanIds(plans).flatMapMany(ids -> summaryRepository.getTopPlansSummaryTrainer(from, to, top, ids));
        }

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "5*month+year+2*totalAmount+3*count+16004")
        public Flux<MonthlyOrderSummary> getOrdersSummaryByMonthBase(LocalDateTime f, LocalDateTime s) {
            return
                    summaryRepository.getOrdersSummaryByDateRangeGroupedByMonth(f, s);

        }


        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "3*month+year+2*totalAmount+3*count+21005")
        public Flux<MonthlyOrderSummary> getOrdersPlanSummaryByMonthBase(LocalDateTime f, LocalDateTime s) {

            return
                    summaryRepository.getAdminOrdersPlanSummaryByDateRangeGroupedByMonth(f, s);

        }

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "2*day+7*month+year+26006")
        public Flux<DailyOrderSummary> getOrdersSummaryByDay(LocalDateTime f, LocalDateTime s) {


            return
                    summaryRepository.getOrdersSummaryByDateRangeGroupedByDay(f, s);
        }

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "4*day+3*month+year+31007")
        public Flux<DailyOrderSummary> getOrdersPlanSummaryByDay(LocalDateTime f, LocalDateTime s) {

            return
                    summaryRepository.getAdminOrdersPlanSummaryByDateRangeGroupedByDay(f, s);
        }

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "5*month+year+36008", masterId = "#trainerId")
        public Flux<MonthlyOrderSummary> getTrainerOrdersSummaryByMonthBase(Pair<LocalDateTime, LocalDateTime> intervalDates, Long trainerId) {
            return summaryRepository.getTrainerOrdersSummaryByDateRangeGroupedByMonth(intervalDates.getFirst(), intervalDates.getSecond(), trainerId);
        }

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "2*day+7*month+year+41009", masterId = "#trainerId")
        public Flux<DailyOrderSummary> getTrainerOrdersSummaryByDayBase(Pair<LocalDateTime, LocalDateTime> intervalDates, Long trainerId) {
            return summaryRepository.getTrainerOrdersSummaryByDateRangeGroupedByDay(intervalDates.getFirst(), intervalDates.getSecond(), trainerId);
        }

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "9*month+year+46001", masterId = "#trainerId")
        Flux<MonthlyOrderSummaryObjective> getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectivesBase(Pair<LocalDateTime, LocalDateTime> intervalDates, Long trainerId) {
            return summaryRepository.getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectives(intervalDates.getFirst(), intervalDates.getSecond(), trainerId);
        }

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "7*month+year+51002", masterId = "#trainerId")
        Flux<MonthlyOrderSummaryType> getTrainerOrdersSummaryByDateRangeGroupedByMonthTypesBase(Pair<LocalDateTime, LocalDateTime> intervalDates, Long trainerId) {
            return summaryRepository.getTrainerOrdersSummaryByDateRangeGroupedByMonthTypes(intervalDates.getFirst(), intervalDates.getSecond(), trainerId);
        }


        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "5*month+year+56003")
        Flux<MonthlyOrderSummaryObjectiveType> getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectivesTypesBase(Pair<LocalDateTime, LocalDateTime> intervalDates, Long trainerId) {
            return summaryRepository.getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectivesTypes(intervalDates.getFirst(), intervalDates.getSecond(), trainerId);
        }

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "userId+rank*3+61004")
        public Flux<TopTrainersSummaryResponse> getTopTrainersSummaryBase(LocalDateTime from, LocalDateTime to, int top) {
            return summaryRepository.getTopTrainersSummary(from, to, top)
                    .map(r -> TopTrainersSummaryResponse.fromR2dbc(r, objectMapper));
        }

        @RedisReactiveChildCache(key = OrderCacheKeys.CACHE_KEY_PATH, idPath = "id")
        public Flux<CountryOrderSummary> getOrdersSummaryByCountryBase(CountrySummaryType type, LocalDate from, LocalDate to) {

            return
                    (type.equals(CountrySummaryType.COUNT) ?
                            summaryRepository.getOrdersCountByCountry(from, to) :
                            summaryRepository.getOrdersTotalByCountry(from, to))
                            .map(e -> {
                                e.setId(
                                        new Locale.Builder().setRegion(e.getId().toUpperCase()).build().getISO3Country()
                                );
                                return e;
                            });

        }

        private Mono<Long[]> getPlanIds(Flux<PlanResponse> plans) {
            return plans.map(PlanResponse::getId).collectList()
                    .map(l -> l.toArray(new Long[0]));
        }
    }

}
