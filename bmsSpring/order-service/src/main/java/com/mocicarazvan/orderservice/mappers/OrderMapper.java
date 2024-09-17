package com.mocicarazvan.orderservice.mappers;

import com.mocicarazvan.orderservice.dtos.OrderDto;
import com.mocicarazvan.orderservice.models.Order;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderMapper {

    public OrderDto fromModelToDto(Order order) {
        return OrderDto.builder()
                .addressId(order.getAddressId())
                .planIds(order.getPlanIds())
                .total(order.getTotal())
                .userId(order.getUserId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .id(order.getId())
                .stripeInvoiceId(order.getStripeInvoiceId())
                .build();
    }

    public Order fromRowToModel(Row row) {
        return Order.builder()
                .addressId(row.get("address_id", Long.class))
                .planIds(EntitiesUtils.convertArrayToList(row.get("plan_ids", Long[].class)))
                .total(EntitiesUtils.getDoubleValue(row, "total"))
                .userId(row.get("user_id", Long.class))
                .id(row.get("id", Long.class))
                .createdAt(row.get("created_at", LocalDateTime.class))
                .updatedAt(row.get("updated_at", LocalDateTime.class))
                .stripeInvoiceId(row.get("stripe_invoice_id", String.class))
                .build();
    }
}
