package com.mocicarazvan.orderservice.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderWithAddress {
    private Order order;
    private CustomAddress address;

    public OrderWithAddress(Long orderId, Long userId, List<Long> planIds, double total,
                            Long addressId, String city, String country, String line1,
                            String line2, String postalCode, String state,
                            LocalDateTime orderCreatedAt, LocalDateTime orderUpdatedAt,
                            LocalDateTime addressCreatedAt, LocalDateTime addressUpdatedAt) {
        this.order = Order.builder()
                .id(orderId)
                .userId(userId)
                .planIds(planIds)
                .total(total)
                .addressId(addressId)
                .createdAt(orderCreatedAt)
                .updatedAt(orderUpdatedAt)
                .build();
        this.address = CustomAddress.builder()
                .id(addressId)
                .city(city)
                .country(country)
                .line1(line1)
                .line2(line2)
                .postalCode(postalCode)
                .state(state)
                .createdAt(addressCreatedAt)
                .updatedAt(addressUpdatedAt)
                .build();
    }
}
