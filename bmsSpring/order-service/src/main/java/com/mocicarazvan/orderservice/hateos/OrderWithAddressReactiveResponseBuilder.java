package com.mocicarazvan.orderservice.hateos;


import com.mocicarazvan.orderservice.controllers.OrderWithAddressController;
import com.mocicarazvan.orderservice.dtos.OrderDtoWithAddress;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrderWithAddressReactiveResponseBuilder extends ReactiveResponseBuilder<OrderDtoWithAddress, OrderWithAddressController> {

    public OrderWithAddressReactiveResponseBuilder() {
        super(new OrdersWithAddressReactiveLinkBuilder());
    }
}
