package com.mocicarazvan.planservice.models;

import com.mocicarazvan.ollamasearch.models.EmbedModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Table(name = "plan_embedding")
public class PlanEmbedding extends EmbedModel {
}
