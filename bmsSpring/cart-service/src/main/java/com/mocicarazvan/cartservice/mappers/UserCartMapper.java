package com.mocicarazvan.cartservice.mappers;

import com.mocicarazvan.cartservice.dtos.UserCartBody;
import com.mocicarazvan.cartservice.dtos.UserCartResponse;
import com.mocicarazvan.cartservice.models.UserCart;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class UserCartMapper extends DtoMapper<UserCart, UserCartBody, UserCartResponse> {
    @Override
    public UserCartResponse fromModelToResponse(UserCart userCart) {
        return UserCartResponse.builder()
                .userId(userCart.getUserId())
                .id(userCart.getId())
                .createdAt(userCart.getCreatedAt())
                .updatedAt(userCart.getUpdatedAt())
                .build();
    }

    @Override
    public UserCart fromBodyToModel(UserCartBody userCartBody) {
        return UserCart.builder()
                .planIds(userCartBody.getPlanIds())
                .build();
    }

    @Override
    public Mono<UserCart> updateModelFromBody(UserCartBody userCartBody, UserCart userCart) {
        Set<Long> planIds = new LinkedHashSet<>(userCart.getPlanIds());
        planIds.addAll(userCartBody.getPlanIds());
        userCart.setPlanIds(planIds.stream().toList());
        userCart.setUpdatedAt(LocalDateTime.now());
        return Mono.just(userCart);
    }
}
