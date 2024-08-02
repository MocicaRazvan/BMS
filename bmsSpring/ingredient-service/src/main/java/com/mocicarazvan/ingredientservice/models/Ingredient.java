package com.mocicarazvan.ingredientservice.models;

import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("ingredient")
public class Ingredient extends ManyToOneUser {
    private String name;

    private DietType type;
    
    private boolean display;
}
