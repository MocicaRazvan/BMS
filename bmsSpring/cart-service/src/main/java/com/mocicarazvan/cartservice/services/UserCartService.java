package com.mocicarazvan.cartservice.services;

import com.mocicarazvan.cartservice.dtos.CartDeletedResponse;
import com.mocicarazvan.cartservice.dtos.UserCartBody;
import com.mocicarazvan.cartservice.dtos.UserCartResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface UserCartService {
    Mono<UserCartResponse> getOrCreate(Long userId);

    Mono<UserCartResponse> addToCart(UserCartBody userCartBody, Long userId);

    Mono<UserCartResponse> removeFromCart(UserCartBody userCartBody, Long userId);

    Mono<UserCartResponse> deleteCartWithNewCreated(Long userId);

    Mono<CartDeletedResponse> deleteCart(Long userId);

    Mono<Void> deleteOldCars(LocalDateTime updatedAtIsLessThan);

    Mono<Long> countAll();
}
