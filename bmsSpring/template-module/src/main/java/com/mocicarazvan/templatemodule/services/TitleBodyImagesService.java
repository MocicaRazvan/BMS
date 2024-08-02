package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.TitleBodyImages;
import com.mocicarazvan.templatemodule.repositories.TitleBodyImagesRepository;
import com.mocicarazvan.templatemodule.repositories.TitleBodyRepository;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TitleBodyImagesService<MODEL extends TitleBodyImages, BODY, RESPONSE extends WithUserDto,
        S extends TitleBodyImagesRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>>
        extends TitleBodyService<MODEL, BODY, RESPONSE, S, M> {
    Mono<RESPONSE> createModel(Flux<FilePart> images, BODY body, String userId);


    Mono<RESPONSE> updateModelWithImages(Flux<FilePart> images, Long id, BODY body, String userId);
}
