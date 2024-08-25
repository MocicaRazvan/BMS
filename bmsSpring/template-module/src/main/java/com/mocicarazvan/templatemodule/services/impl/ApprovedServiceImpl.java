package com.mocicarazvan.templatemodule.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.clients.CountInParentClient;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyDto;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserLikesAndDislikes;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.Approve;
import com.mocicarazvan.templatemodule.models.TitleBody;
import com.mocicarazvan.templatemodule.repositories.ApprovedRepository;
import com.mocicarazvan.templatemodule.services.ApprovedService;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class ApprovedServiceImpl<MODEL extends Approve, BODY extends TitleBodyDto, RESPONSE extends WithUserDto,
        S extends ApprovedRepository<MODEL>, M extends DtoMapper<MODEL, BODY, RESPONSE>>
        extends TitleBodyImagesServiceImpl<MODEL, BODY, RESPONSE, S, M>
        implements ApprovedService<MODEL, BODY, RESPONSE, S, M> {
    public ApprovedServiceImpl(S modelRepository, M modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, String modelName, List<String> allowedSortingFields, EntitiesUtils entitiesUtils, FileClient fileClient, ObjectMapper objectMapper) {
        super(modelRepository, modelMapper, pageableUtils, userClient, modelName, allowedSortingFields, entitiesUtils, fileClient, objectMapper);
    }

    @Override
    public Mono<ResponseWithUserDto<RESPONSE>> approveModel(Long id, String userId, boolean approved) {
//        return getModel(id)
//                .flatMap(model -> {
//                    if (model.isApproved() && approved) {
//                        return Mono.error(new IllegalActionException(modelName + " with id " + id + " is already approved!"));
//                    }
//                    model.setApproved(approved);
//                    return modelRepository.save(model);
//                })
//                .map(modelMapper::fromModelToResponse)
//                ;
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
                        .flatMap(model -> {
                                    if (model.isApproved() && approved) {
                                        return Mono.error(new IllegalActionException(modelName + " with id " + id + " is already approved!"));
                                    }
                                    model.setApproved(approved);
                                    return modelRepository.save(model).flatMap(m -> getModelGuardWithUser(authUser, m, !m.isApproved()));
                                }

                        )
                );

    }

    @Override
    public Flux<PageableResponse<RESPONSE>> getModelsApproved(PageableBody pageableBody, String userId) {
        return getModelsTitle(null, true, pageableBody, userId);
    }

    @Override
    public Flux<PageableResponse<RESPONSE>> getModelsApproved(String title, PageableBody pageableBody, String userId) {
        return getModelsTitle(title, true, pageableBody, userId);
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<RESPONSE>>> getModelsWithUser(String title, PageableBody pageableBody, String userId, boolean approved) {
        return getModelsTitle(title, approved, pageableBody, userId)
                .concatMap(this::getPageableWithUser);
    }

//    public Mono<PageableResponse<ResponseWithUserDto<RESPONSE>>> getPageableWithUser(PageableResponse<RESPONSE> pr) {
//        return userClient.getUser("", String.valueOf(pr.getContent().getUserId()))
//                .map(userDto -> ResponseWithUserDto.<RESPONSE>builder()
//                        .model(pr.getContent())
//                        .user(userDto)
//                        .build())
//                .map(ru -> PageableResponse.<ResponseWithUserDto<RESPONSE>>builder()
//                        .content(ru)
//                        .pageInfo(pr.getPageInfo())
//                        .build());
//    }

    @Override
    public Flux<PageableResponse<RESPONSE>> getAllModels(String title, PageableBody pageableBody, String userId) {
        final String newTitle = title == null ? "" : title.trim();
        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(pr -> pageableUtils.createPageableResponse(
                                modelRepository.findAllByTitleContainingIgnoreCase(newTitle, pr).map(modelMapper::fromModelToResponse),
                                modelRepository.countAllByTitleContainingIgnoreCase(newTitle),
                                pr
                        )
                );
    }


    @Override
    public Mono<RESPONSE> getModelById(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
                        .flatMap(model -> getResponseGuard(authUser, model, !model.isApproved()))
                );
    }


    @Override
    public Mono<ResponseWithUserDto<RESPONSE>> getModelByIdWithUser(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
                        .flatMap(model -> getModelGuardWithUser(authUser, model, !model.isApproved()))
                );

    }

    public Flux<PageableResponse<RESPONSE>> getModelsTitle(String title, boolean approved, PageableBody pageableBody, String userId) {

        final String newTitle = title == null ? "" : title.trim();

        return
                protectRoute(approved, pageableBody, userId)
                        .flatMapMany(pr -> pageableUtils.createPageableResponse(
                                modelRepository.findAllByTitleContainingIgnoreCaseAndApproved(newTitle, approved, pr).map(modelMapper::fromModelToResponse),
                                modelRepository.countAllByTitleContainingIgnoreCaseAndApproved(newTitle, approved),
                                pr
                        ));
    }

    protected Mono<PageRequest> protectRoute(boolean approved, PageableBody pageableBody, String userId) {
        return userClient.getUser("", userId).flatMap(
                        u -> {
                            if (!u.getRole().equals(Role.ROLE_ADMIN) && !approved) {
                                return Mono.error(new PrivateRouteException());
                            }
                            return Mono.just(u);
                        }
                )
                .then(pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields))
                .then(pageableUtils.createPageRequest(pageableBody));
    }

    @Override
    public Mono<ResponseWithUserLikesAndDislikes<RESPONSE>> getModelByIdWithUserLikesAndDislikes(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
                        .flatMap(model -> getModelGuardWithLikesAndDislikes(authUser, model, !model.isApproved()))
                );
    }


    @Override
    public Flux<PageableResponse<RESPONSE>> getModelsTrainer(String title, Long trainerId, PageableBody pageableBody, String userId, Boolean approved) {
        String newTitle = title == null ? "" : title.trim();
//        return userClient.existsTrainerOrAdmin("/exists", trainerId)
//                .then(pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields))
//                .then(pageableUtils.createPageRequest(pageableBody))
//                .flatMapMany(pr -> userClient.getUser("", userId)
//                        .flatMapMany(authUser -> privateRoute(true, authUser, trainerId))
//                        .thenMany(pageableUtils.createPageableResponse(
//                                (approved == null ? modelRepository.findAllByUserIdAndTitleContainingIgnoreCase(trainerId, newTitle, pr)
//                                        : modelRepository.findAllByUserIdAndTitleContainingIgnoreCaseAndApproved(trainerId, newTitle, approved, pr)
//                                ).map(modelMapper::fromModelToResponse),
//                                approved == null ? modelRepository.countAllByUserIdAndTitleContainingIgnoreCase(trainerId, newTitle)
//                                        : modelRepository.countAllByUserIdAndTitleContainingIgnoreCaseAndApproved(trainerId, newTitle, approved),
//                                pr
//                        )));
        return getModelsAuthor(trainerId, pageableBody, userId, pr -> (pageableUtils.createPageableResponse(
                (approved == null ? modelRepository.findAllByUserIdAndTitleContainingIgnoreCase(trainerId, newTitle, pr)
                        : modelRepository.findAllByUserIdAndTitleContainingIgnoreCaseAndApproved(trainerId, newTitle, approved, pr)
                ).map(modelMapper::fromModelToResponse),
                approved == null ? modelRepository.countAllByUserIdAndTitleContainingIgnoreCase(trainerId, newTitle)
                        : modelRepository.countAllByUserIdAndTitleContainingIgnoreCaseAndApproved(trainerId, newTitle, approved),
                pr
        )));
    }

    protected Flux<PageableResponse<RESPONSE>> getModelsAuthor(Long trainerId, PageableBody pageableBody, String userId,
                                                               Function<PageRequest, Flux<PageableResponse<RESPONSE>>> getResponse) {
        return userClient.existsTrainerOrAdmin("/exists", trainerId)
                .then(pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields))
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(pr -> userClient.getUser("", userId)
                        .flatMapMany(authUser -> privateRoute(true, authUser, trainerId))
                        .thenMany(getResponse.apply(pr)));
    }

    public Mono<Tuple2<RESPONSE, UserDto>> getModelByIdWithOwner(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
                        .flatMap(model -> getResponseGuard(authUser, model, !model.isApproved())
                                .flatMap(response -> userClient.getUser("", model.getUserId().toString())
                                        .map(user -> Tuples.of(response, user))
                                )
                        )

                );


    }

//    protected Mono<PageableResponse<ResponseWithEntityCount<RESPONSE>>> toResponseWithCount(String userId, CountInParentClient client, PageableResponse<RESPONSE> pr) {
//        return client.getCountInParent(pr.getContent().getId(), userId)
//                .map(entityCount -> PageableResponse.<ResponseWithEntityCount<RESPONSE>>builder()
//                        .content(ResponseWithEntityCount.of(pr.getContent(), entityCount))
//                        .pageInfo(pr.getPageInfo())
//                        .links(pr.getLinks())
//                        .build());
//    }
}
