package com.mocicarazvan.templatemodule.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.files.MetadataDto;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.FileUploadResponse;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.TitleBodyImages;
import com.mocicarazvan.templatemodule.repositories.TitleBodyImagesRepository;
import com.mocicarazvan.templatemodule.services.TitleBodyImagesService;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public abstract class TitleBodyImagesServiceImpl<MODEL extends TitleBodyImages, BODY, RESPONSE extends WithUserDto,
        S extends TitleBodyImagesRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>>
        extends TitleBodyServiceImpl<MODEL, BODY, RESPONSE, S, M>
        implements TitleBodyImagesService<MODEL, BODY, RESPONSE, S, M> {

    protected final FileClient fileClient;
    protected final ObjectMapper objectMapper;
    protected final TitleBodyImagesServiceCacheHandler<MODEL, BODY, RESPONSE> titleBodyImagesServiceCacheHandler;

    public TitleBodyImagesServiceImpl(S modelRepository, M modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, String modelName, List<String> allowedSortingFields, EntitiesUtils entitiesUtils, FileClient fileClient, ObjectMapper objectMapper, TitleBodyImagesServiceCacheHandler<MODEL, BODY, RESPONSE> titleBodyImagesServiceCacheHandler) {
        super(modelRepository, modelMapper, pageableUtils, userClient, modelName, allowedSortingFields, entitiesUtils, titleBodyImagesServiceCacheHandler);
        this.fileClient = fileClient;
        this.objectMapper = objectMapper;
        this.titleBodyImagesServiceCacheHandler = titleBodyImagesServiceCacheHandler;
    }

    @Override
    public Mono<RESPONSE> createModel(Flux<FilePart> images, BODY body, String userId, String clientId) {
        return
                titleBodyImagesServiceCacheHandler.createModelInvalidate.apply(
                        getModelToBeCreatedWithImages(images, body, userId, clientId)
                                .flatMap(modelRepository::save)
                                .map(modelMapper::fromModelToResponse), body, userId);
    }

    @Override
    public Mono<RESPONSE> updateModelWithImages(Flux<FilePart> images, Long id, BODY body, String userId, String clientId) {
        return
                titleBodyImagesServiceCacheHandler.updateModelInvalidate.apply(
                        updateModelWithSuccess(id, userId, model -> fileClient.deleteFiles(model.getImages())
                                .then(uploadFiles(images, FileType.IMAGE, clientId)
                                        .flatMap(fileUploadResponse -> modelMapper.updateModelFromBody(body, model)
                                                .map(m -> {
                                                    m.setImages(fileUploadResponse.getFiles());
                                                    return m;
                                                })
                                        )
                                )), id, body, userId);

    }


    @Override
    public Mono<RESPONSE> deleteModel(Long id, String userId) {
        return
                titleBodyImagesServiceCacheHandler.deleteModelInvalidate.apply(
                        userClient.getUser("", userId)
                                .flatMap(authUser -> getModel(id)
                                        .flatMap(model -> privateRoute(true, authUser, model.getUserId()).thenReturn(model)
                                                .flatMap(m -> fileClient.deleteFiles(m.getImages()))
                                                .then(modelRepository.delete(model))
                                                .then(Mono.fromCallable(() -> modelMapper.fromModelToResponse(model)))
                                        )
                                ), id, userId);
    }

//    protected Mono<FileUploadResponse> uploadImages(Flux<FilePart> images) {
//        MetadataDto metadataDto = new MetadataDto();
//        metadataDto.setName("images " + modelName);
//        metadataDto.setFileType(FileType.IMAGE);
//        return fileClient.uploadFiles(images, metadataDto, objectMapper);
//    }

    protected Mono<FileUploadResponse> uploadFiles(Flux<FilePart> files, FileType fileType, String clientId) {
        MetadataDto metadataDto = new MetadataDto();
        metadataDto.setName(fileType.toString() + " " + modelName);
        metadataDto.setFileType(fileType);
        metadataDto.setClientId(clientId);
        return fileClient.uploadFiles(files, metadataDto, objectMapper);
    }

    protected Mono<MODEL> getModelToBeCreatedWithImages(Flux<FilePart> images, BODY body, String userId, String clientId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> uploadFiles(images, FileType.IMAGE, clientId)
                        .map(fileUploadResponse -> {
                            MODEL model = modelMapper.fromBodyToModel(body);
                            model.setImages(fileUploadResponse.getFiles());
                            model.setUserId(authUser.getId());
                            model.setCreatedAt(LocalDateTime.now());
                            model.setUpdatedAt(LocalDateTime.now());
                            if (model.getUserDislikes() == null)
                                model.setUserDislikes(List.of());
                            if (model.getUserLikes() == null)
                                model.setUserLikes(List.of());
                            return model;
                        }));
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
//    @SuperBuilder
//    @AllArgsConstructor
    public static class TitleBodyImagesServiceCacheHandler<MODEL extends TitleBodyImages, BODY, RESPONSE extends WithUserDto>
            extends TitleBodyServiceImpl.TitleBodyServiceCacheHandler<MODEL, BODY, RESPONSE> {


        public TitleBodyImagesServiceCacheHandler() {
            super();
        }
    }
}
