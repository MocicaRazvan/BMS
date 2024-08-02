package com.mocicarazvan.websocketservice.dtos.recipe;

import com.mocicarazvan.websocketservice.dtos.generic.ApproveResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
//@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RecipeResponse extends ApproveResponse {
}
