package com.mocicarazvan.orderservice.dtos;

import com.mocicarazvan.orderservice.enums.DietType;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class RecipeResponse extends ApproveDto {
    private List<String> videos;

    private DietType type;
}
