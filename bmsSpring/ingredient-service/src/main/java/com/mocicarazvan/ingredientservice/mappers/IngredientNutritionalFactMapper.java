package com.mocicarazvan.ingredientservice.mappers;


import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactResponse;
import com.mocicarazvan.ingredientservice.dtos.IngredientResponse;
import com.mocicarazvan.ingredientservice.dtos.NutritionalFactResponse;
import com.mocicarazvan.ingredientservice.models.Ingredient;
import com.mocicarazvan.ingredientservice.models.IngredientNutritionalFact;
import com.mocicarazvan.ingredientservice.models.NutritionalFact;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngredientNutritionalFactMapper {

    private final IngredientMapper ingredientMapper;
    private final NutritionalFactMapper nutritionalFactMapper;

    public IngredientNutritionalFactResponse fromModelsToResponse(Ingredient ingredient, NutritionalFact nutritionalFact) {
        return IngredientNutritionalFactResponse.builder()
                .ingredient(ingredientMapper.fromModelToResponse(ingredient))
                .nutritionalFact(nutritionalFactMapper.fromModelToResponse(nutritionalFact))
                .build();
    }

    public IngredientNutritionalFactResponse fromResponsesToResponse(IngredientResponse ingredientResponse, NutritionalFactResponse nutritionalFactResponse) {
        return IngredientNutritionalFactResponse.builder()
                .ingredient(ingredientResponse)
                .nutritionalFact(nutritionalFactResponse)
                .build();
    }

    public IngredientNutritionalFact formModelsToModel(Ingredient ingredient, NutritionalFact nutritionalFact) {
        return IngredientNutritionalFact.builder()
                .ingredient(ingredient)
                .nutritionalFact(nutritionalFact)
                .build();
    }

    public IngredientNutritionalFactResponse fromModelToResponse(IngredientNutritionalFact ingredientNutritionalFact) {
        return fromModelsToResponse(ingredientNutritionalFact.getIngredient(), ingredientNutritionalFact.getNutritionalFact());
    }

    public IngredientNutritionalFact fromRowToModel(Row row) {
        return formModelsToModel(
                ingredientMapper.fromRowToModel(row),
                nutritionalFactMapper.fromRowToModel(row)
        );
    }
}
