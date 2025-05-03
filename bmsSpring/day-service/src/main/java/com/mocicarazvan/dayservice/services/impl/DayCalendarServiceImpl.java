package com.mocicarazvan.dayservice.services.impl;

import com.mocicarazvan.dayservice.clients.RecipeClient;
import com.mocicarazvan.dayservice.dtos.dayCalendar.*;
import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
import com.mocicarazvan.dayservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.dayservice.mappers.DayCalendarMapper;
import com.mocicarazvan.dayservice.models.DayCalendar;
import com.mocicarazvan.dayservice.repositories.DayCalendarRepository;
import com.mocicarazvan.dayservice.services.DayCalendarService;
import com.mocicarazvan.dayservice.services.DayService;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithChildList;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.print.PrinterException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class DayCalendarServiceImpl implements DayCalendarService {
    private static final String CACHE_KEY = "dayCalendar";

    private final DayCalendarMapper dayCalendarMapper;
    private final UserClient userClient;
    private final DayCalendarRepository dayCalendarRepository;
    private final RecipeClient recipeClient;
    private final DayCalendarServiceCacheHandler self;
    private final DayService dayService;

    @RedisReactiveChildCacheEvict(key = CACHE_KEY, masterId = "#userId")
    @Override
    public Mono<DayCalendarResponse<MealResponse>> createDayCalendar(DayCalendarBody dayCalendarBody, String userId) {
        Long userIdLong = Long.parseLong(userId);
        return getUser(userIdLong)
                .then(Mono.defer(() -> {
                    DayCalendar dayCalendar = dayCalendarMapper.fromBodyToModel(dayCalendarBody);
                    dayCalendar.setUserId(userIdLong);
                    return dayCalendarRepository.save(dayCalendar)
                            .flatMap(
                                    saved -> dayCalendarRepository.findFullDayCalendarByIdAndUserId(
                                            saved.getId(),
                                            userIdLong
                                    )
                            ).map(
                                    dayCalendarMapper::fromDbDtoToResponse
                            )
                            .flatMap(this::addAuthorToResponse
                            );
                }));
    }

    @RedisReactiveChildCacheEvict(key = CACHE_KEY, masterId = "#userId", id = "#id")
    @Override
    public Mono<Boolean> deleteDayCalendar(Long id, String userId) {
        Long userIdLong = Long.parseLong(userId);
        return getUser(userIdLong)
                .then(Mono.defer(() -> dayCalendarRepository.findById(id)
                        .switchIfEmpty(Mono.error(new NotFoundEntity("DayCalendar", id)))
                        .filter(dayCalendar -> Objects.equals(dayCalendar.getUserId(), userIdLong))
                        .switchIfEmpty(Mono.error(new PrinterException()))
                        .flatMap(dayCalendar ->
                                dayCalendarRepository.delete(dayCalendar)
                                        .thenReturn(true)
                        )));

    }

    @Override
    public Mono<DayCalendarResponse<MealResponse>> getDayCalendarById(Long id, String userId) {
        Long userIdLong = Long.parseLong(userId);
        return getUser(userIdLong)
                .then(Mono.defer(() -> self.getDayCalendarSimpleById(id, userId)
                        .flatMap(this::createFromDaySimpleResponse
                        )

                ));
    }

    @RedisReactiveChildCache(key = CACHE_KEY, masterId = CACHE_KEY, idPath = "userId * 10000 + month + year * 100")
    @Override
    public Flux<DayCalendarTrackingStats> getDayCalendarTrackingStats(String userId, LocalDate from, LocalDate to) {
        LocalDateTime dateAfter = from == null ? null : from.atStartOfDay();
        LocalDateTime dateBefore = to == null ? null : to.atTime(LocalTime.MAX);
        return dayCalendarRepository.findDayCalendarTrackingStats(
                Long.parseLong(userId),
                dateAfter,
                dateBefore
        );
    }

    private Mono<DayCalendarResponse<MealResponse>> createFromDaySimpleResponse(DayCalendarSimpleResponse simpleResponse) {
        return dayService.getDayResponseWithMeals(simpleResponse.getDayId())
                .map(dayResponseWithMeals ->
                        {
                            DayCalendarResponse<MealResponse> dayCalendarResponse = DayCalendarResponse.fromSimpleResponse(simpleResponse);
                            dayCalendarResponse.setDayResponse(dayResponseWithMeals);
                            dayCalendarResponse.setMealResponses(dayResponseWithMeals.getMealResponses());
                            return dayCalendarResponse;

                        }
                )
                .flatMap(this::addAuthorToResponse);
    }

    private Mono<DayCalendarResponse<MealResponse>> addAuthorToResponse(DayCalendarResponse<MealResponse> r) {
        return userClient.getUser("", String.valueOf(r.getDayResponse().getUserId()))
                .map(userDto -> {
                    r.setAuthor(userDto);
                    return r;
                });
    }

    @Override
    public Flux<DayCalendarResponse<MealResponse>> getDayCalendarsByUserIdAndDateBetween(
            String userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Long userIdLong = Long.parseLong(userId);
        return getUser(userIdLong)
                .thenMany(self.getDaysByRangeSimple(userId, startDate, endDate)
                )
                .collectList()
                .filter(sr -> !sr.isEmpty())
                .flatMapMany(sr -> {
                    List<Long> dayIds = sr.stream().map(DayCalendarSimpleResponse::getDayId).toList();
                    return dayService.getDaysWithMeals(dayIds)
                            .collectMap(IdGenerateDto::getId)
                            .flatMapMany(
                                    dr -> Flux.fromIterable(
                                            sr
                                    ).map(s -> {
                                        DayCalendarResponse<MealResponse> dayCalendarResponse = DayCalendarResponse.fromSimpleResponse(s);
                                        dayCalendarResponse.setDayResponse(dr.get(s.getDayId()));
                                        dayCalendarResponse.setMealResponses(dr.get(s.getDayId()).getMealResponses());
                                        return dayCalendarResponse;
                                    })
                            ).flatMap(this::addAuthorToResponse);

                });
    }

    @Override
    public Flux<DayCalendarResponse<ResponseWithChildList<MealResponse, RecipeResponse>>> getFullDayCalendarsByUserIdAndDateBetween(
            String userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return getDayCalendarsByUserIdAndDateBetween(userId, startDate, endDate)
                .flatMap(
                        resp -> Flux.fromIterable(
                                        resp.getMealResponses()
                                )
                                .flatMap(
                                        mr -> recipeClient.getByIds(
                                                        mr.getRecipes().stream().map(Object::toString).toList(),
                                                        userId
                                                )
                                                .collectList()
                                                .map(r -> Map.entry(mr.getId(), r))
                                )
                                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                                .map(m -> {
                                    List<ResponseWithChildList<MealResponse, RecipeResponse>> meals =
                                            resp.getMealResponses()
                                                    .stream().map(
                                                            mr -> new ResponseWithChildList<>(
                                                                    mr,
                                                                    m.get(mr.getId())
                                                            )
                                                    ).toList();

                                    return resp.cloneWithMeals(meals);
                                })

                );
    }

    @Override
    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUserForDayCalendar(
            Long dcId, Long dayId, Long recipeId, String userId
    ) {
        return dayCalendarRepository.existsByIdAndUserIdAndDayId(dcId,
                Long.parseLong(userId),
                dayId
        ).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new PrivateRouteException());
            }
            return recipeClient.getByIdWithUser(String.valueOf(recipeId), userId);
        });
    }


    @Override
    @RedisReactiveChildCache(key = CACHE_KEY, masterId = "#userId", idPath = "id")
    public Flux<DayCalendarUserDates> getAllDaysByUserId(String userId) {
        return dayCalendarRepository.findAllUserDatesByUserId(Long.parseLong(userId));
    }

    private Mono<Void> getUser(Long userId) {
        return userClient.existsUser("/exists", String.valueOf(userId));
    }

    @Component
    @RequiredArgsConstructor
    private static class DayCalendarServiceCacheHandler {
        private final DayCalendarRepository dayCalendarRepository;
        private final DayCalendarMapper dayCalendarMapper;

        @RedisReactiveChildCache(key = CACHE_KEY, id = "#id", masterId = "#userId")
        public Mono<DayCalendarSimpleResponse> getDayCalendarSimpleById(Long id, String userId) {
            return dayCalendarRepository.findById(id)
                    .map(dayCalendarMapper::fromModelToSimpleResponse);
        }

        @RedisReactiveChildCache(key = CACHE_KEY, masterId = "#userId", idPath = "id")
        public Flux<DayCalendarSimpleResponse> getDaysByRangeSimple(
                String userId,
                LocalDate start,
                LocalDate end
        ) {
            return dayCalendarRepository.findAllByUserIdAndDateBetween(
                            Long.parseLong(userId),
                            start,
                            end
                    )
                    .map(dayCalendarMapper::fromModelToSimpleResponse);
        }

    }
}
