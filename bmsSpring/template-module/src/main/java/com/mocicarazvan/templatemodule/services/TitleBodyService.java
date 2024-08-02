package com.mocicarazvan.templatemodule.services;


import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserLikesAndDislikes;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.TitleBody;
import com.mocicarazvan.templatemodule.repositories.TitleBodyRepository;
import reactor.core.publisher.Mono;

public interface TitleBodyService<MODEL extends TitleBody, BODY, RESPONSE extends WithUserDto,
        S extends TitleBodyRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>>
        extends ManyToOneUserService<MODEL, BODY, RESPONSE, S, M> {

    Mono<RESPONSE> reactToModel(Long id, String type, String userId);

    Mono<ResponseWithUserLikesAndDislikes<RESPONSE>> getModelByIdWithUserLikesAndDislikes(Long id, String userId);


}
