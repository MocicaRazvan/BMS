package com.mocicarazvan.postservice.repositories;

import com.mocicarazvan.ollamasearch.repositories.EmbedModelRepository;
import com.mocicarazvan.postservice.models.PostEmbedding;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface PostEmbeddingRepository extends EmbedModelRepository<PostEmbedding> {

    @Override
    @Modifying
    @Query("""
            update  post_embedding
             set embedding = :embedding, updated_at = now()
             where entity_id = :entityId
            """)
    Mono<Boolean> updateEmbeddingByEntityId(Long entityId, Float[] embedding);
}
