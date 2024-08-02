package com.mocicarazvan.userservice.repositories;

import com.mocicarazvan.userservice.models.OauthState;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface OauthStateRepository extends R2dbcRepository<OauthState, Long> {
    Mono<OauthState> findByState(String state);

    Mono<Void> deleteByState(String state);
}