package com.mocicarazvan.ingredientservice.mappers;

import com.mocicarazvan.ingredientservice.dtos.NutritionalFactBody;
import com.mocicarazvan.ingredientservice.dtos.NutritionalFactResponse;
import com.mocicarazvan.ingredientservice.enums.UnitType;
import com.mocicarazvan.ingredientservice.models.NutritionalFact;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import io.r2dbc.spi.Row;
import org.mapstruct.Mapper;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;


@Mapper(componentModel = "spring")
public abstract class NutritionalFactMapper extends DtoMapper<NutritionalFact, NutritionalFactBody, NutritionalFactResponse> {
    @Override
    public NutritionalFactResponse fromModelToResponse(NutritionalFact nutritionalFact) {
        return NutritionalFactResponse.builder()
                .userId(nutritionalFact.getUserId())
                .id(nutritionalFact.getId())
                .createdAt(nutritionalFact.getCreatedAt())
                .updatedAt(nutritionalFact.getUpdatedAt())
                .fat(nutritionalFact.getFat())
                .saturatedFat(nutritionalFact.getSaturatedFat())
                .carbohydrates(nutritionalFact.getCarbohydrates())
                .sugar(nutritionalFact.getSugar())
                .protein(nutritionalFact.getProtein())
                .salt(nutritionalFact.getSalt())
                .unit(nutritionalFact.getUnit())
                .build();
    }

    @Override
    public NutritionalFact fromBodyToModel(NutritionalFactBody nutritionalFactBody) {
        return NutritionalFact.builder()
                .fat(nutritionalFactBody.getFat())
                .saturatedFat(nutritionalFactBody.getSaturatedFat())
                .carbohydrates(nutritionalFactBody.getCarbohydrates())
                .sugar(nutritionalFactBody.getSugar())
                .protein(nutritionalFactBody.getProtein())
                .salt(nutritionalFactBody.getSalt())
                .unit(nutritionalFactBody.getUnit())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public Mono<NutritionalFact> updateModelFromBody(NutritionalFactBody nutritionalFactBody, NutritionalFact nutritionalFact) {
        nutritionalFact.setFat(nutritionalFactBody.getFat());
        nutritionalFact.setSaturatedFat(nutritionalFactBody.getSaturatedFat());
        nutritionalFact.setCarbohydrates(nutritionalFactBody.getCarbohydrates());
        nutritionalFact.setSugar(nutritionalFactBody.getSugar());
        nutritionalFact.setProtein(nutritionalFactBody.getProtein());
        nutritionalFact.setSalt(nutritionalFactBody.getSalt());
        nutritionalFact.setUnit(nutritionalFactBody.getUnit());
        nutritionalFact.setUpdatedAt(LocalDateTime.now());
        return Mono.just(nutritionalFact);
    }

    public NutritionalFact fromRowToModel(Row row) {
        return NutritionalFact.builder()
                .id(row.get("n_id", Long.class))
                .createdAt(row.get("n_created_at", LocalDateTime.class))
                .updatedAt(row.get("n_updated_at", LocalDateTime.class))
                .userId(row.get("n_user_id", Long.class))
                .fat(EntitiesUtils.getDoubleValue(row, "n_fat"))
                .saturatedFat(EntitiesUtils.getDoubleValue(row, "n_saturated_fat"))
                .carbohydrates(EntitiesUtils.getDoubleValue(row, "n_carbohydrates"))
                .sugar(EntitiesUtils.getDoubleValue(row, "n_sugar"))
                .protein(EntitiesUtils.getDoubleValue(row, "n_protein"))
                .salt(EntitiesUtils.getDoubleValue(row, "n_salt"))
                .unit(UnitType.valueOf(Objects.requireNonNull(row.get("n_unit", String.class)).toUpperCase()))
                .ingredientId(row.get("n_ingredient_id", Long.class))
                .build();
    }


}
