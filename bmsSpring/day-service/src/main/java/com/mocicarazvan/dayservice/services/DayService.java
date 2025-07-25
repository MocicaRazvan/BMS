package com.mocicarazvan.dayservice.services;

import com.mocicarazvan.dayservice.dtos.day.DayBody;
import com.mocicarazvan.dayservice.dtos.day.DayBodyWithMeals;
import com.mocicarazvan.dayservice.dtos.day.DayResponse;
import com.mocicarazvan.dayservice.dtos.day.DayResponseWithMeals;
import com.mocicarazvan.dayservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.dayservice.mappers.DayMapper;
import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.dayservice.repositories.DayRepository;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.services.CountInParentService;
import com.mocicarazvan.templatemodule.services.TitleBodyService;
import com.mocicarazvan.templatemodule.services.ValidIds;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface DayService extends TitleBodyService<
        Day, DayBody, DayResponse, DayRepository, DayMapper
        >, CountInParentService, ValidIds<Day, DayRepository, DayResponse> {

    Mono<List<String>> seedEmbeddings();

    Flux<PageableResponse<DayResponse>> getDaysFiltered(String title, DayType type, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                        LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                        PageableBody pageableBody, String userId, Boolean admin);

    Flux<PageableResponse<DayResponse>> getDaysFilteredByIds(String title, DayType type, List<Long> ids, List<Long> excludeIds,
                                                             LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                             LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                             PageableBody pageableBody, String userId, Boolean admin);

    Flux<PageableResponse<ResponseWithUserDto<DayResponse>>> getDaysFilteredWithUser(String title, DayType type, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                     LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageableBody pageableBody, String userId, Boolean admin);

    Flux<PageableResponse<ResponseWithEntityCount<DayResponse>>> getDaysFilteredWithCount(String title, DayType type, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                          LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageableBody pageableBody, String userId, Boolean admin);

    Flux<PageableResponse<DayResponse>> getDaysFilteredTrainer(String title, DayType type, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                               LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageableBody pageableBody, String userId, Long trainerId);

    Flux<PageableResponse<ResponseWithEntityCount<DayResponse>>> getDaysFilteredTrainerWithCount(String title, DayType type, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                                 LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageableBody pageableBody, String userId, Long trainerId);


    Mono<Void> validIds(List<Long> ids);

    Mono<DayResponse> createWithMeals(DayBodyWithMeals dayBodyWithMeals, String userId);

    Mono<DayResponse> updateWithMeals(Long id, DayBodyWithMeals dayBodyWithMeals, String userId);

    Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUserInternal(Long id, Long recipeId, String userId);

    Mono<ResponseWithUserDto<DayResponse>> getModelByIdWithUserInternal(Long id, String userId);

    Mono<DayResponse> getModelById(Long id);

    Mono<DayResponseWithMeals> getDayResponseWithMeals(
            Long id
    );

    Flux<DayResponseWithMeals> getDaysWithMeals(List<Long> ids);
}
