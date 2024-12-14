package com.mocicarazvan.dayservice.models;

import com.mocicarazvan.ollamasearch.models.EmbedModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Table(name = "day_embedding")
public class DayEmbedding extends EmbedModel {
}
