package com.mocicarazvan.templatemodule.repositories;

import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;

@NoRepositoryBean
public interface ManyToOneUserRepository<M extends ManyToOneUser> extends IdGeneratedRepository<M> {
    Flux<M> findAllByUserId(Long userId, PageRequest pageRequest);

    Flux<M> findModelByMonth(int month, int year);
}
