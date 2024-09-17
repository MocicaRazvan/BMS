package com.mocicarazvan.templatemodule.controllers;


import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.TitleBodyImages;
import com.mocicarazvan.templatemodule.repositories.TitleBodyImagesRepository;
import com.mocicarazvan.templatemodule.services.TitleBodyImagesService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TitleBodyImagesController<MODEL extends TitleBodyImages, BODY, RESPONSE extends WithUserDto,
        S extends TitleBodyImagesRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>,
        G extends TitleBodyImagesService<MODEL, BODY, RESPONSE, S, M>>
        extends TitleBodyController<MODEL, BODY, RESPONSE, S, M, G> {

    @PostMapping(value = "/createWithImages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Mono<ResponseEntity<CustomEntityModel<RESPONSE>>> createModelWithImages(@RequestPart("files") Flux<FilePart> files,
                                                                            @RequestPart("body") String body,
                                                                            @RequestParam("clientId") String clientId,
                                                                            ServerWebExchange exchange);

    @PostMapping(value = "/updateWithImages/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Mono<ResponseEntity<CustomEntityModel<RESPONSE>>> updateModelWithImages(@RequestPart("files") Flux<FilePart> files,
                                                                            @RequestPart("body") String body,
                                                                            @RequestParam("clientId") String clientId,
                                                                            @PathVariable Long id,
                                                                            ServerWebExchange exchange
    );
}
