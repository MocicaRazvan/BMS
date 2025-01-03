package com.mocicarazvan.orderservice.services;

import com.mocicarazvan.orderservice.models.CustomAddress;
import com.stripe.model.Address;
import reactor.core.publisher.Mono;

public interface CustomAddressService {

    Mono<CustomAddress> saveAddress(Address address);
}
