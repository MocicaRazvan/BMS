package com.mocicarazvan.recipeservice.repositories;

import com.mocicarazvan.ollamasearch.repositories.EmbedModelRepository;
import com.mocicarazvan.recipeservice.models.RecipeEmbedding;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface RecipeEmbeddingRepository extends EmbedModelRepository<RecipeEmbedding> {
    @Override
    @Modifying
    @Query("""
            update  recipe_embedding
             set embedding = :embedding, updated_at = now()
             where entity_id = :entityId
            """)
    Mono<Boolean> updateEmbeddingByEntityId(Long entityId, Float[] embedding);
}
