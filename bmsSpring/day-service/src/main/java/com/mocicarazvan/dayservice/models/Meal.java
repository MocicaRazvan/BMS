package com.mocicarazvan.dayservice.models;

import com.mocicarazvan.templatemodule.models.ManyToOneUser;
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
@Table("meal")
public class Meal extends ManyToOneUser implements Cloneable {
    private List<Long> recipes;
    private Long dayId;
    private String period;

    @Override
    public Meal clone() {
        Meal clone = (Meal) super.clone();
        clone.setRecipes(List.copyOf(recipes));
        return clone;
    }
}
