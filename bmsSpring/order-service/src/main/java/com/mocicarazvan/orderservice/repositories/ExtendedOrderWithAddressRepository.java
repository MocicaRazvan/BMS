package com.mocicarazvan.orderservice.repositories;

import com.mocicarazvan.orderservice.models.OrderWithAddress;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface ExtendedOrderWithAddressRepository {
    Mono<OrderWithAddress> getModelById(Long id);

    Flux<OrderWithAddress> getModelsFiltered(String city, String state, String country, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                             LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageRequest pageRequest);


    Mono<Long> countModelsFiltered(String city, String state, String country, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                   LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound);

    Flux<OrderWithAddress> getModelsFilteredUser(String city, String state, String country, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                 LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, Long userId, PageRequest pageRequest);

    Mono<Long> countModelsFilteredUser(String city, String state, String country, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                       LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, Long userId);
}
