package com.mocicarazvan.planservice.repositories;

import com.mocicarazvan.ollamasearch.repositories.EmbedModelRepository;
import com.mocicarazvan.planservice.models.PlanEmbedding;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;


public interface PlanEmbeddingRepository extends EmbedModelRepository<PlanEmbedding> {

    @Override
    @Modifying
    @Query("""
            update  plan_embedding
             set embedding = :embedding, updated_at = now()
             where entity_id = :entityId
            """)
    Mono<Boolean> updateEmbeddingByEntityId(Long entityId, Float[] embedding);
}
