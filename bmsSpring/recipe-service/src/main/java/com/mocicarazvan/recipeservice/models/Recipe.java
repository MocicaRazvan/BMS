package com.mocicarazvan.recipeservice.models;

import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.templatemodule.models.Approve;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("recipe")
public class Recipe extends Approve {
    private List<String> videos;

    private DietType type;
}
