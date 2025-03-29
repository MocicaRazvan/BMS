package com.mocicarazvan.dayservice.services.impl;

import com.mocicarazvan.dayservice.clients.RecipeClient;
import com.mocicarazvan.dayservice.dtos.day.DayBodyWithMeals;
import com.mocicarazvan.dayservice.dtos.day.DayResponseWithMeals;
import com.mocicarazvan.dayservice.dtos.meal.ComposeMealBody;
import com.mocicarazvan.dayservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.dayservice.services.DayService;
import com.mocicarazvan.templatemodule.services.MockItemService;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Service
public class MockDayService extends MockItemService<DayResponseWithMeals> {
    private final RecipeClient recipeClient;
    private final DayService dayService;

    protected MockDayService(TransactionalOperator transactionalOperator, RecipeClient recipeClient, DayService dayService) {
        super(transactionalOperator, 5);
        this.recipeClient = recipeClient;
        this.dayService = dayService;

    }

    @Override
    protected Mono<Pair<DayResponseWithMeals, List<FilePart>>> mockItemsBase(Long itemId, String userId) {
        return dayService.getDayResponseWithMeals(itemId)
                .flatMap(dm ->
                        Flux.fromIterable(dm.getMealResponses())
                                .index()
                                .flatMap(
                                        m ->
                                                Flux.fromIterable(m.getT2().getRecipes())
                                                        .flatMap(r -> recipeClient.getClient()
                                                                .post()
                                                                .uri(uriBuilder -> uriBuilder.path("/mock/" + r).build())
                                                                .accept(MediaType.APPLICATION_NDJSON)
                                                                .header(RequestsUtils.AUTH_HEADER, userId)
                                                                .retrieve()
                                                                .bodyToFlux(new ParameterizedTypeReference<RecipeResponse>() {
                                                                            }
                                                                )
                                                                .map(RecipeResponse::getId)
                                                                .collectList()
                                                                .map(rs -> Pair.of(m.getT1(), new ComposeMealBody(rs, m.getT2().getPeriod()))))
                                )
                                .collectSortedList(Comparator.comparing(Pair::getFirst))
                                .flatMap(pairs ->
                                        Flux.fromIterable(pairs)
                                                .map(Pair::getSecond)
                                                .collectList()
                                )
                                .flatMap(meals -> {
                                    DayBodyWithMeals dayBody = DayBodyWithMeals.builder()
                                            .meals(meals)
                                            .type(dm.getType())
                                            .title(createTitle(dm.getTitle()))
                                            .body(dm.getBody())
                                            .build();
                                    return dayService.createWithMeals(
                                                    dayBody,
                                                    userId
                                            )
                                            .flatMap(d -> dayService.getDayResponseWithMeals(d.getId()));
                                })
                                .map(d -> Pair.of(d, List.of()))
                );
    }
}
