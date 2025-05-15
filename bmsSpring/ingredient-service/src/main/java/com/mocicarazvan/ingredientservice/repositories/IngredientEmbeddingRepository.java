package com.mocicarazvan.ingredientservice.repositories;

import com.mocicarazvan.ingredientservice.models.IngredientEmbedding;
import com.mocicarazvan.ollamasearch.repositories.EmbedModelRepository;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface IngredientEmbeddingRepository extends EmbedModelRepository<IngredientEmbedding> {

    @Override
    @Modifying
    @Query("""
            update  ingredient_embedding
             set embedding = :embedding, updated_at = now()
             where entity_id = :entityId
            """)
    Mono<Boolean> updateEmbeddingByEntityId(Long entityId, Float[] embedding);
}
