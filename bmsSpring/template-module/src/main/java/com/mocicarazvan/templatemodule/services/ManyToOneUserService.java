package com.mocicarazvan.templatemodule.services;


import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ManyToOneUserService<MODEL extends ManyToOneUser, BODY, RESPONSE extends WithUserDto,
        S extends ManyToOneUserRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>> {
    Mono<RESPONSE> deleteModel(Long id, String userId);

    Mono<RESPONSE> getModelById(Long id, String userId);

    Flux<PageableResponse<RESPONSE>> getAllModels(PageableBody pageableBody, String userId);

    Mono<RESPONSE> updateModel(Long id, BODY body, String userId);

    Mono<ResponseWithUserDto<RESPONSE>> getModelByIdWithUser(Long id, String userId);


    Flux<ResponseWithUserDto<RESPONSE>> getModelsWithUser(List<Long> ids, String userId);

    Flux<PageableResponse<RESPONSE>> getModelsByIdInPageable(List<Long> ids, PageableBody pageableBody);

    Flux<RESPONSE> getModelsByIdIn(List<Long> ids);

    Mono<RESPONSE> createModel(BODY body, String userId);

    Flux<MonthlyEntityGroup<RESPONSE>> getModelGroupedByMonth(int month, String userId);

    Mono<MODEL> getModel(Long id);
}
