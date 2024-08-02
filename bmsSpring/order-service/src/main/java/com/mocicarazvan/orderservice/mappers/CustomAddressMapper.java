package com.mocicarazvan.orderservice.mappers;


import com.mocicarazvan.orderservice.dtos.CustomAddressDto;
import com.mocicarazvan.orderservice.models.CustomAddress;
import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CustomAddressMapper {

    public CustomAddressDto fromModelToDto(CustomAddress customAddress) {
        return CustomAddressDto.builder()
                .createdAt(customAddress.getCreatedAt())
                .updatedAt(customAddress.getUpdatedAt())
                .id(customAddress.getId())
                .city(customAddress.getCity())
                .country(customAddress.getCountry())
                .line1(customAddress.getLine1())
                .line2(customAddress.getLine2())
                .postalCode(customAddress.getPostalCode())
                .state(customAddress.getState())
                .build();
    }

    public CustomAddress fromRowToModel(Row row) {
        return CustomAddress.builder()
                .city(row.get("a_city", String.class))
                .country(row.get("a_country", String.class))
                .line1(row.get("a_line1", String.class))
                .line2(row.get("a_line2", String.class))
                .postalCode(row.get("a_postal_code", String.class))
                .state(row.get("a_state", String.class))
                .id(row.get("a_id", Long.class))
                .createdAt(row.get("a_created_at", LocalDateTime.class))
                .updatedAt(row.get("a_updated_at", LocalDateTime.class))
                .build();
    }
}
