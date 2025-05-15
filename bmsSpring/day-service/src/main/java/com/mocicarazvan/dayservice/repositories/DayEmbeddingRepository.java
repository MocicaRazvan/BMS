package com.mocicarazvan.dayservice.repositories;

import com.mocicarazvan.dayservice.models.DayEmbedding;
import com.mocicarazvan.ollamasearch.repositories.EmbedModelRepository;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface DayEmbeddingRepository extends EmbedModelRepository<DayEmbedding> {

    @Override
    @Modifying
    @Query("""
            update  day_embedding
             set embedding = :embedding, updated_at = now()
             where entity_id = :entityId
            """)
    Mono<Boolean> updateEmbeddingByEntityId(Long entityId, Float[] embedding);
}
