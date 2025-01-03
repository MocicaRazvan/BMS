package com.mocicarazvan.ingredientservice.models;

import com.mocicarazvan.ingredientservice.enums.UnitType;
import com.mocicarazvan.templatemodule.models.ManyToOneUser;
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
@Table("nutritional_fact")
public class NutritionalFact extends ManyToOneUser implements Cloneable {
    @Column("fat")
    private double fat;

    @Column("saturated_fat")
    private double saturatedFat;

    @Column("carbohydrates")
    private double carbohydrates;

    @Column("sugar")
    private double sugar;

    @Column("protein")
    private double protein;

    @Column("salt")
    private double salt;

    @Column("unit")
    private UnitType unit;

    @Column("ingredient_id")
    private Long ingredientId;

    @Override
    public NutritionalFact clone() {
        return (NutritionalFact) super.clone();
    }
}
