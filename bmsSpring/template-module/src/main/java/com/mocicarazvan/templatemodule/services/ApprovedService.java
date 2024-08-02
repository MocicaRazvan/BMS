package com.mocicarazvan.templatemodule.services;


import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyDto;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.Approve;
import com.mocicarazvan.templatemodule.models.TitleBody;
import com.mocicarazvan.templatemodule.repositories.ApprovedRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApprovedService<MODEL extends Approve, BODY extends TitleBodyDto, RESPONSE extends WithUserDto,
        S extends ApprovedRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>>
        extends TitleBodyImagesService<MODEL, BODY, RESPONSE, S, M> {
    Mono<ResponseWithUserDto<RESPONSE>> approveModel(Long id, String userId, boolean approved);

    Flux<PageableResponse<RESPONSE>> getModelsApproved(PageableBody pageableBody, String userId);

    Flux<PageableResponse<RESPONSE>> getModelsApproved(String title, PageableBody pageableBody, String userId);

    Flux<PageableResponse<ResponseWithUserDto<RESPONSE>>> getModelsWithUser(String title, PageableBody pageableBody, String userId, boolean approved);

    Flux<PageableResponse<RESPONSE>> getModelsTrainer(String title, Long trainerId, PageableBody pageableBody, String userId, Boolean approved);

    Flux<PageableResponse<RESPONSE>> getAllModels(String title, PageableBody pageableBody, String userId);


//    Flux<PageableResponse<RESPONSE>> getModelsTitle(String title, boolean approved, PageableBody pageableBody);
}
