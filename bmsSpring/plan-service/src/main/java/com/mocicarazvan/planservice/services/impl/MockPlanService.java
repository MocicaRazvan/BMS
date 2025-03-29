package com.mocicarazvan.planservice.services.impl;

import com.mocicarazvan.planservice.clients.DayClient;
import com.mocicarazvan.planservice.dtos.PlanBody;
import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.planservice.dtos.dayClient.DayResponse;
import com.mocicarazvan.planservice.enums.ObjectiveType;
import com.mocicarazvan.planservice.mappers.PlanMapper;
import com.mocicarazvan.planservice.repositories.PlanRepository;
import com.mocicarazvan.planservice.services.PlanService;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.services.MockItemService;
import com.mocicarazvan.templatemodule.utils.OrderEnsurer;
import com.mocicarazvan.templatemodule.utils.Randomizer;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class MockPlanService extends MockItemService<PlanResponse> {
    private final PlanService planService;
    private final DayClient dayClient;
    private final PlanMapper planMapper;
    private final PlanRepository planRepository;
    private static final List<ObjectiveType> objectiveTypes = List.of(ObjectiveType.values());

    protected MockPlanService(TransactionalOperator transactionalOperator, PlanService planService, DayClient dayClient, PlanMapper planMapper, PlanRepository planRepository) {
        super(transactionalOperator, 5);
        this.planService = planService;
        this.dayClient = dayClient;
        this.planMapper = planMapper;
        this.planRepository = planRepository;
    }

    @Override
    protected Mono<Pair<PlanResponse, List<FilePart>>> mockItemsBase(Long itemId, String userId) {
        return planService.getModel(itemId)
                .flatMap(plan ->
                        Flux.fromIterable(plan.getDays())
                                .flatMap(d ->
                                        dayClient.getClient()
                                                .post()
                                                .uri(uriBuilder -> uriBuilder.path("/mock/" + d).build())
                                                .accept(MediaType.APPLICATION_NDJSON)
                                                .header(RequestsUtils.AUTH_HEADER, userId)
                                                .retrieve()
                                                .bodyToFlux(new ParameterizedTypeReference<DayResponse>() {
                                                })
                                                .map(dr -> Pair.of(d, dr.getId()))
                                )
                                .transform(f -> OrderEnsurer.orderFlux(f, plan.getDays(), Pair::getFirst))
                                .map(Pair::getSecond)
                                .collectList()
                                .zipWith(getFiles(plan.getImages(), FileType.IMAGE))
                                .flatMap(t -> {
                                    List<Long> days = t.getT1();
                                    List<FilePart> files = t.getT2();
                                    PlanBody planBody = PlanBody.builder()
                                            .type(plan.getType())
                                            .objective(Randomizer.pickRandomItemFromList(objectiveTypes))
                                            .price(Math.round(Randomizer.addRandomNumber(plan.getPrice(), 15) * 100.0) / 100.0)
                                            .days(days)
                                            .body(plan.getBody())
                                            .title(createTitle(plan.getTitle()))
                                            .build();
                                    return planService.createModel(
                                                    Flux.fromIterable(Randomizer.shuffleList(files))
                                                    , planBody, userId,
                                                    UUID.randomUUID().toString()
                                            )
                                            .flatMap(p ->
                                                    planRepository.findById(p.getId())
                                                            .flatMap(plan1 -> {
                                                                plan1.setApproved(true);
                                                                return planRepository.save(plan1)
                                                                        .map(planMapper::fromModelToResponse);
                                                            })
                                            )
                                            .map(p -> Pair.of(p, files));
                                })
                );

    }
}
