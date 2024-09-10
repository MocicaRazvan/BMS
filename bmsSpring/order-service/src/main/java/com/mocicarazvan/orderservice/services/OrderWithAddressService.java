package com.mocicarazvan.orderservice.services;

import com.mocicarazvan.orderservice.dtos.OrderDtoWithAddress;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderWithAddressService {
    Mono<OrderDtoWithAddress> getModelById(Long id, String userId);

    Flux<PageableResponse<OrderDtoWithAddress>> getModelsFilteredAdmin(String city, String state, String country, PageableBody pageableBody, String userId);

    Flux<PageableResponse<OrderDtoWithAddress>> getModelsFilteredUser(String city, String state, String country, PageableBody pageableBody, Long userId, String authUserId);
}
