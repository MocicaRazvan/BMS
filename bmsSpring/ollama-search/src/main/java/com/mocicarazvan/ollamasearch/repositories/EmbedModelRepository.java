package com.mocicarazvan.ollamasearch.repositories;

import com.mocicarazvan.ollamasearch.models.EmbedModel;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface EmbedModelRepository<T extends EmbedModel> extends ReactiveCrudRepository<T, Long> {
    Mono<Void> deleteByEntityId(Long entityId);

    /**
     * Updates the embedding vector for the specified entity.
     *
     * <p>Executes an UPDATE on the <code>item_embedding</code> table, setting the
     * <code>embedding</code> column and refreshing <code>updated_at</code> to the current timestamp.
     *
     * <p>SQL:
     * <pre>
     * UPDATE item_embedding
     *   SET embedding   = :embedding,
     *       updated_at  = NOW()
     * WHERE entity_id   = :entityId;
     * </pre>
     *
     * @param entityId  the unique identifier of the entity whose embedding is being updated
     * @param embedding the new embedding vector to persist
     * @return a {@link reactor.core.publisher.Mono} that emits {@code true} if the update succeeded,
     * or {@code false} if no row was updated
     */
    @Modifying
    Mono<Boolean> updateEmbeddingByEntityId(
            Long entityId,
            Float[] embedding);

}
