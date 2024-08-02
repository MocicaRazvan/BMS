package com.mocicarazvan.orderservice.mappers;

import com.mocicarazvan.orderservice.dtos.OrderDtoWithAddress;
import com.mocicarazvan.orderservice.models.CustomAddress;
import com.mocicarazvan.orderservice.models.Order;
import com.mocicarazvan.orderservice.models.OrderWithAddress;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderWithAddressMapper {

    private final OrderMapper orderMapper;
    private final CustomAddressMapper customAddressMapper;

    public OrderDtoWithAddress fromModelsToDto(Order order, CustomAddress customAddress) {
        return OrderDtoWithAddress.builder()
                .order(orderMapper.fromModelToDto(order))
                .address(customAddressMapper.fromModelToDto(customAddress))
                .build();
    }

    public OrderDtoWithAddress fromModelToDto(OrderWithAddress orderWithAddress) {
        return fromModelsToDto(orderWithAddress.getOrder(), orderWithAddress.getAddress());
    }

    public OrderWithAddress fromRow(Row row) {
        return OrderWithAddress.builder()
                .order(orderMapper.fromRowToModel(row))
                .address(customAddressMapper.fromRowToModel(row))
                .build();
    }
}
