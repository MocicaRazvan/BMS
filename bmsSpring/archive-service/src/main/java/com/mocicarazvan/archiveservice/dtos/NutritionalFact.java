package com.mocicarazvan.archiveservice.dtos;


import com.mocicarazvan.archiveservice.dtos.enums.UnitType;
import com.mocicarazvan.archiveservice.dtos.generic.ManyToOneUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NutritionalFact extends ManyToOneUser {
    private double fat;

    private double saturatedFat;

    private double carbohydrates;

    private double sugar;


    private double protein;

    private double salt;

    private UnitType unit;

    private Long ingredientId;

    @Override
    public String toString() {
        return "NutritionalFact{" + "id=" + getId() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                "userId=" + getUserId() +
                "fat=" + fat +
                ", saturatedFat=" + saturatedFat +
                ", carbohydrates=" + carbohydrates +
                ", sugar=" + sugar +
                ", protein=" + protein +
                ", salt=" + salt +
                ", unit=" + unit +
                ", ingredientId=" + ingredientId +
                '}';
    }

}
