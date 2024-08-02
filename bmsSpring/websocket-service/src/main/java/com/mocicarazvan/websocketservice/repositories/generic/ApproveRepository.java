package com.mocicarazvan.websocketservice.repositories.generic;

import com.mocicarazvan.websocketservice.models.generic.ApprovedModel;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;


@NoRepositoryBean
public interface ApproveRepository<M extends ApprovedModel> extends IdGeneratedRepository<M> {
    Optional<M> findByAppId(Long appId);
}
