package com.mocicarazvan.orderservice.services.impl;

import com.mocicarazvan.orderservice.models.CustomAddress;
import com.mocicarazvan.orderservice.repositories.CustomAddressRepository;
import com.mocicarazvan.orderservice.services.CustomAddressService;
import com.stripe.model.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class CustomAddressServiceImpl implements CustomAddressService {
    private final CustomAddressRepository customAddressRepository;

    @Override
    public Mono<CustomAddress> saveAddress(Address address) {
        return customAddressRepository.save(CustomAddress.fromStripe(address));
    }
}
