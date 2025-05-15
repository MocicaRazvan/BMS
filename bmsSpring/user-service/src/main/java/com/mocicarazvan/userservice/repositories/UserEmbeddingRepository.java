package com.mocicarazvan.userservice.repositories;

import com.mocicarazvan.ollamasearch.repositories.EmbedModelRepository;
import com.mocicarazvan.userservice.models.UserEmbedding;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface UserEmbeddingRepository extends EmbedModelRepository<UserEmbedding> {
    @Override
    @Modifying
    @Query("""
            update  user_embedding
             set embedding = :embedding, updated_at = now()
             where entity_id = :entityId
            """)
    Mono<Boolean> updateEmbeddingByEntityId(Long entityId, Float[] embedding);
}
