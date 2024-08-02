package com.mocicarazvan.templatemodule.hateos.controller;


import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.BiFunction;


@RequiredArgsConstructor
public class ReactiveResponseBuilder<RESPONSE, C> {

    protected final ReactiveLinkBuilder<RESPONSE, C> linkBuilder;

    public Mono<CustomEntityModel<RESPONSE>> toModel(RESPONSE response, Class<C> clazz) {
        CustomEntityModel<RESPONSE> model = CustomEntityModel.of(response);
        List<Mono<Link>> links = linkBuilder.createModelLinks(response, clazz)
                .stream().map(WebFluxLinkBuilder.WebFluxLink::toMono).toList();

        return Flux.merge(links)
                .collectList()
                .doOnNext(model::add)
                .thenReturn(model);

    }

    public Mono<ResponseWithUserDtoEntity<RESPONSE>> toModelWithUser(ResponseWithUserDto<RESPONSE> response, Class<C> clazz) {
        return toModel(response.getModel(), clazz)
                .map(model -> {
                            ResponseWithUserDtoEntity<RESPONSE> entity = new ResponseWithUserDtoEntity<>();
                            entity.setUser(response.getUser());
                            entity.setModel(model);
                            return entity;
                        }
                );
    }

    public Mono<ResponseWithUserLikesAndDislikesEntity<RESPONSE>> toModelWithUserLikesAndDislikes(ResponseWithUserLikesAndDislikes<RESPONSE> response, Class<C> clazz) {
        return toModelWithUser(response, clazz)
                .map(withUser -> {
                            ResponseWithUserLikesAndDislikesEntity<RESPONSE> entity = new ResponseWithUserLikesAndDislikesEntity<>();
                            entity.setUserLikes(response.getUserLikes());
                            entity.setUserDislikes(response.getUserDislikes());
                            entity.setUser(withUser.getUser());
                            entity.setModel(withUser.getModel());
                            return entity;
                        }
                );
    }

    public <CHILD> Mono<ResponseWithChildListUser<RESPONSE, CHILD>> toModelWithChildListUser(ResponseWithChildList<ResponseWithUserDto<RESPONSE>, CHILD> response, Class<C> clazz) {
        return toModelWithUser(response.getEntity(), clazz)
                .map(withUser -> {
                            ResponseWithChildListUser<RESPONSE, CHILD> entity = new ResponseWithChildListUser<>();
                            entity.setEntity(withUser);
                            entity.setChildren(response.getChildren());
                            return entity;
                        }
                );

    }

    public Mono<ResponseWithEntityCount<CustomEntityModel<RESPONSE>>> toModelWithEntityCount(ResponseWithEntityCount<RESPONSE> response, Class<C> clazz) {
        return toModel(response.getModel(), clazz)
                .map(model -> {
                    ResponseWithEntityCount<CustomEntityModel<RESPONSE>> entity = new ResponseWithEntityCount<>();
                    entity.setModel(model);
                    entity.setCount(response.getCount());
                    return entity;
                });
    }

    public Mono<PageableResponse<CustomEntityModel<RESPONSE>>> toModelPageable(PageableResponse<RESPONSE> pageableResponse, Class<C> clazz) {
        return toModelGeneric(pageableResponse, clazz, this::toModel);
    }

    public Mono<PageableResponse<ResponseWithUserDtoEntity<RESPONSE>>> toModelWithUserPageable(PageableResponse<ResponseWithUserDto<RESPONSE>> response, Class<C> clazz) {
        return toModelGeneric(response, clazz, this::toModelWithUser);
    }

    public Mono<PageableResponse<ResponseWithUserLikesAndDislikesEntity<RESPONSE>>> toModelWithUserLikesAndDislikesPageable(PageableResponse<ResponseWithUserLikesAndDislikes<RESPONSE>> response, Class<C> clazz) {
        return toModelGeneric(response, clazz, this::toModelWithUserLikesAndDislikes);
    }

    public <CHILD> Mono<PageableResponse<ResponseWithChildListUser<RESPONSE, CHILD>>> toModelWithChildListUserPageable(PageableResponse<ResponseWithChildList<ResponseWithUserDto<RESPONSE>, CHILD>> response, Class<C> clazz) {
        return toModelGeneric(response, clazz, this::toModelWithChildListUser);
    }

    public Mono<PageableResponse<ResponseWithEntityCount<CustomEntityModel<RESPONSE>>>> toModelWithEntityCountPageable(PageableResponse<ResponseWithEntityCount<RESPONSE>> response, Class<C> clazz) {
        return toModelGeneric(response, clazz, this::toModelWithEntityCount);
    }


    private <T, R> Mono<PageableResponse<R>> toModelGeneric(
            PageableResponse<T> response,
            Class<C> clazz,
            BiFunction<T, Class<C>, Mono<R>> conversionFunction) {

        return conversionFunction.apply(response.getContent(), clazz)
                .map(c -> PageableResponse.<R>builder()
                        .content(c)
                        .pageInfo(response.getPageInfo())
                        .build());
    }

    public Mono<MonthlyEntityGroup<CustomEntityModel<RESPONSE>>> toModelMonthlyEntityGroup(MonthlyEntityGroup<RESPONSE> monthlyEntityGroup, Class<C> clazz) {
        return toModel(monthlyEntityGroup.getEntity(), clazz)
                .map(model -> MonthlyEntityGroup.<CustomEntityModel<RESPONSE>>builder()
                        .entity(model)
                        .month(monthlyEntityGroup.getMonth())
                        .year(monthlyEntityGroup.getYear())
                        .build());
    }


}
