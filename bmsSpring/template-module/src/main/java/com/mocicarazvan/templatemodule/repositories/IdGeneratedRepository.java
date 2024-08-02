package com.mocicarazvan.templatemodule.repositories;

import com.mocicarazvan.templatemodule.models.IdGenerated;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@NoRepositoryBean
public interface IdGeneratedRepository<M extends IdGenerated> extends R2dbcRepository<M, Long> {
    Flux<M> findAllBy(PageRequest pageRequest);

    Flux<M> findAllByIdIn(List<Long> ids, PageRequest pageRequest);

    Flux<M> findAllByIdIn(List<Long> ids);

    Mono<Long> countAllByIdIn(List<Long> ids);

}
