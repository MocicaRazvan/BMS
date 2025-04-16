package com.mocicarazvan.templatemodule.hateos.controller;


import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;


@RequiredArgsConstructor
public class ReactiveResponseBuilder<RESPONSE, C> {

    protected final ReactiveLinkBuilder<RESPONSE, C> linkBuilder;

    public Mono<CustomEntityModel<RESPONSE>> toModel(RESPONSE response, Class<C> clazz) {
        CustomEntityModel<RESPONSE> model = CustomEntityModel.of(response);

        return Flux.fromIterable(linkBuilder.createModelLinks(response, clazz))
                .flatMap(WebFluxLinkBuilder.WebFluxLink::toMono)
                .collectList()
                .doOnNext(model::add)
                .thenReturn(model);

    }

    public <P> Mono<Pair<CustomEntityModel<RESPONSE>, P>> toModelWithPair(Pair<RESPONSE, P> response, Class<C> clazz) {
        return toModel(response.getFirst(), clazz)
                .map(model -> Pair.of(model, response.getSecond()));
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

    public <CHILD> Mono<ResponseWithChildListEntity<RESPONSE, CHILD>> toModelWithChildListEntity(ResponseWithChildList<RESPONSE, CHILD> response, Class<C> clazz) {
        return toModel(response.getEntity(), clazz)
                .map(model -> {
                    ResponseWithChildListEntity<RESPONSE, CHILD> entity = new ResponseWithChildListEntity<>();
                    entity.setEntity(model);
                    entity.setChildren(response.getChildren());
                    return entity;
                });
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
        return toModelGeneric(pageableResponse, clazz, this::toModel)
                .transform(m -> addLinksToPage(m,
                        pr -> pr.getContent().get_links().values()
                ));
    }

    private <T> Mono<PageableResponse<T>> addLinksToPage(Mono<PageableResponse<T>> pageableResponse,
                                                         Function<PageableResponse<T>, Collection<Link>> linksFunction
    ) {
        return
                pageableResponse.map(pr -> {
                    pr.setLinks(new ArrayList<>(linksFunction.apply(pr)));
                    return pr;
                });
    }

    public Mono<PageableResponse<ResponseWithUserDtoEntity<RESPONSE>>> toModelWithUserPageable(PageableResponse<ResponseWithUserDto<RESPONSE>> response, Class<C> clazz) {
        return toModelGeneric(response, clazz, this::toModelWithUser)
                .transform(m -> addLinksToPage(m,
                        pr -> pr.getContent().getModel().get_links().values()
                ));
    }

    public Mono<PageableResponse<ResponseWithUserLikesAndDislikesEntity<RESPONSE>>> toModelWithUserLikesAndDislikesPageable(PageableResponse<ResponseWithUserLikesAndDislikes<RESPONSE>> response, Class<C> clazz) {
        return toModelGeneric(response, clazz, this::toModelWithUserLikesAndDislikes)
                .transform(m -> addLinksToPage(m,
                        pr -> pr.getContent().getModel().get_links().values()
                ));
    }

    public <CHILD> Mono<PageableResponse<ResponseWithChildListUser<RESPONSE, CHILD>>> toModelWithChildListUserPageable(PageableResponse<ResponseWithChildList<ResponseWithUserDto<RESPONSE>, CHILD>> response, Class<C> clazz) {
        return toModelGeneric(response, clazz, this::toModelWithChildListUser)
                .transform(m -> addLinksToPage(m,
                        pr -> pr.getContent().getEntity().getModel().get_links().values()
                ));
    }

    public Mono<PageableResponse<ResponseWithEntityCount<CustomEntityModel<RESPONSE>>>> toModelWithEntityCountPageable(PageableResponse<ResponseWithEntityCount<RESPONSE>> response, Class<C> clazz) {
        return toModelGeneric(response, clazz, this::toModelWithEntityCount)
                .transform(m -> addLinksToPage(m,
                        pr -> pr.getContent().getModel().get_links().values()
                ));
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

    public <T> Mono<CustomEntityModel<T>> toModelConvertSetContent(RESPONSE response, Class<C> clazz, T content) {
        return toModel(response, clazz)
                .map(model -> model.convertContent(content));
    }


}
