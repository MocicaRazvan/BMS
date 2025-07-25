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
public class Recipe extends Approve implements Cloneable {
    private List<String> videos;

    private DietType type;

    @Override
    public Recipe clone() {
        Recipe clone = (Recipe) super.clone();
        clone.setVideos(List.copyOf(videos));
        return clone;
    }
}
