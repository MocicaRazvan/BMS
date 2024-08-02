package com.mocicarazvan.userservice.repositories;

import com.mocicarazvan.userservice.enums.OTPType;
import com.mocicarazvan.userservice.models.OTPToken;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface OTPTokenRepository extends R2dbcRepository<OTPToken, Long> {
    Mono<Boolean> existsByUserId(long userId);

    Mono<OTPToken> findByUserIdAndTokenAndType(long userId, String token, OTPType type);

    Mono<Void> deleteAllByUserIdAndType(long userId, OTPType type);
}
