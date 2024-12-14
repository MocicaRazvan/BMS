package com.mocicarazvan.ollamasearch.repositories;

import com.mocicarazvan.ollamasearch.models.EmbedModel;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface EmbedModelRepository<T extends EmbedModel> extends ReactiveCrudRepository<T, Long> {
    Mono<Void> deleteByEntityId(Long entityId);

}
