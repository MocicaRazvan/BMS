package com.mocicarazvan.planservice.mappers;


import com.mocicarazvan.planservice.dtos.PlanBody;
import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.planservice.dtos.PlanResponseWithSimilarity;
import com.mocicarazvan.planservice.dtos.PlanWithSimilarity;
import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.enums.ObjectiveType;
import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;

@Component

public class PlanMapper extends DtoMapper<Plan, PlanBody, PlanResponse> {
    @Override
    public PlanResponse fromModelToResponse(Plan plan) {
        return PlanResponse.builder()
                .price(plan.getPrice())
                .type(plan.getType())
                .display(plan.isDisplay())
                .days(plan.getDays())
                .approved(plan.isApproved())
                .images(plan.getImages())
                .body(plan.getBody())
                .title(plan.getTitle())
                .userLikes(plan.getUserLikes())
                .userDislikes(plan.getUserDislikes())
                .userId(plan.getUserId())
                .id(plan.getId())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .objective(plan.getObjective())
                .build();
    }

    @Override
    public Plan fromBodyToModel(PlanBody planBody) {
        return Plan.builder()
                .type(planBody.getType())
                .price(planBody.getPrice())
                .days(planBody.getDays())
                .body(planBody.getBody())
                .title(planBody.getTitle())
                .objective(planBody.getObjective())
                .approved(false)
                .display(false)
                .build();
    }

    @Override
    public Mono<Plan> updateModelFromBody(PlanBody planBody, Plan plan) {
        plan.setType(planBody.getType());
        plan.setPrice(planBody.getPrice());
        plan.setDays(planBody.getDays());
        plan.setTitle(planBody.getTitle());
        plan.setBody(planBody.getBody());
        plan.setObjective(planBody.getObjective());
        plan.setApproved(false);
        plan.setDisplay(false);
        plan.setUpdatedAt(LocalDateTime.now());
        return Mono.just(plan);
    }

    public Plan fromRowToModel(Row row) {
        return Plan.builder()
                .price(EntitiesUtils.getDoubleValue(row, "price"))
                .days(EntitiesUtils.convertArrayToList(row.get("days", Long[].class)))
                .type(DietType.valueOf(Objects.requireNonNull(row.get("type", String.class)).toUpperCase()))
                .objective(ObjectiveType.valueOf(Objects.requireNonNull(row.get("objective", String.class)).toUpperCase()))
                .display(Boolean.TRUE.equals(row.get("display", Boolean.class)))
                .approved(Boolean.TRUE.equals(row.get("approved", Boolean.class)))
                .images(EntitiesUtils.convertArrayToList(row.get("images", String[].class)))
                .title(row.get("title", String.class))
                .body(row.get("body", String.class))
                .userLikes(EntitiesUtils.convertArrayToList(row.get("user_likes", Long[].class)))
                .userDislikes(EntitiesUtils.convertArrayToList(row.get("user_dislikes", Long[].class)))
                .userId(row.get("user_id", Long.class))
                .id(row.get("id", Long.class))
                .createdAt(row.get("created_at", LocalDateTime.class))
                .updatedAt(row.get("updated_at", LocalDateTime.class))
                .build();
    }

    public PlanResponseWithSimilarity fromPlanWithSimilarityToResponse(PlanWithSimilarity planWithSimilarity) {
        return PlanResponseWithSimilarity.builder()
                .price(planWithSimilarity.getPrice())
                .type(planWithSimilarity.getType())
                .display(planWithSimilarity.isDisplay())
                .days(planWithSimilarity.getDays())
                .approved(planWithSimilarity.isApproved())
                .images(planWithSimilarity.getImages())
                .body(planWithSimilarity.getBody())
                .title(planWithSimilarity.getTitle())
                .userLikes(planWithSimilarity.getUserLikes())
                .userDislikes(planWithSimilarity.getUserDislikes())
                .userId(planWithSimilarity.getUserId())
                .id(planWithSimilarity.getId())
                .createdAt(planWithSimilarity.getCreatedAt())
                .updatedAt(planWithSimilarity.getUpdatedAt())
                .objective(planWithSimilarity.getObjective())
                .similarity(planWithSimilarity.getSimilarity())
                .build();
    }
}
