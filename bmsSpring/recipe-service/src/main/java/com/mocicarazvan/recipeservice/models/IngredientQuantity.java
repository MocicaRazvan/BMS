package com.mocicarazvan.recipeservice.models;

import com.mocicarazvan.templatemodule.models.IdGenerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("ingredient_quantity")
public class IngredientQuantity extends IdGenerated implements Cloneable {
    private double quantity;

    @Column("ingredient_id")
    private Long ingredientId;

    @Column("recipe_id")
    private Long recipeId;

    @Override
    public IngredientQuantity clone() {
        return (IngredientQuantity) super.clone();
    }
}
