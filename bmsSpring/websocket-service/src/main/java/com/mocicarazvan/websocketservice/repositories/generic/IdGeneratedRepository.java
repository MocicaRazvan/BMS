package com.mocicarazvan.websocketservice.repositories.generic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IdGeneratedRepository<M> extends JpaRepository<M, Long> {
}
