package com.mocicarazvan.templatemodule.mappers;


import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import reactor.core.publisher.Mono;

public abstract class DtoMapper<MODEL extends ManyToOneUser, BODY, RESPONSE extends WithUserDto> {

    public abstract RESPONSE fromModelToResponse(MODEL model);

    public abstract MODEL fromBodyToModel(BODY body);

    public abstract Mono<MODEL> updateModelFromBody(BODY body, MODEL model);

}
