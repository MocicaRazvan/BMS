package com.mocicarazvan.userservice.models;

import com.mocicarazvan.ollamasearch.models.EmbedModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Table(name = "user_embedding")
public class UserEmbedding extends EmbedModel {
}
