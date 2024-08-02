package com.mocicarazvan.orderservice.repositories;

import com.mocicarazvan.orderservice.models.OrderWithAddress;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExtendedOrderWithAddressRepository {
    Mono<OrderWithAddress> getModelById(Long id);

    Flux<OrderWithAddress> getModelsFiltered(String city, String state, String country, PageRequest pageRequest);


    Mono<Long> countModelsFiltered(String city, String state, String country);

    Flux<OrderWithAddress> getModelsFilteredUser(String city, String state, String country, Long userId, PageRequest pageRequest);

    Mono<Long> countModelsFilteredUser(String city, String state, String country, Long userId);
}
