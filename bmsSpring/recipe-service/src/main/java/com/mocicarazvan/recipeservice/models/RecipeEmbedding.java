package com.mocicarazvan.recipeservice.models;

import com.mocicarazvan.ollamasearch.models.EmbedModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Table(name = "recipe_embedding")
public class RecipeEmbedding extends EmbedModel {
}
