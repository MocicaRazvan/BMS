package com.mocicarazvan.cartservice.repositories;

import com.mocicarazvan.cartservice.models.UserCart;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface UserCartRepository extends R2dbcRepository<UserCart, Long> {

    Mono<Boolean> existsByUserId(Long userId);

    Mono<UserCart> findByUserId(Long userId);

    Mono<Void> deleteByUserId(Long userId);

    Mono<Void> deleteAllByUpdatedAtLessThan(LocalDateTime updatedAtIsLessThan);

    Mono<Long> countAllBy();
}
