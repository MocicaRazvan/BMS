package com.mocicarazvan.orderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderDtoWithAddress {
    private CustomAddressDto address;
    private OrderDto order;
}
