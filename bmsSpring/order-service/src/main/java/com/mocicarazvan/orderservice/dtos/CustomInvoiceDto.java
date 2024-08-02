package com.mocicarazvan.orderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomInvoiceDto {
    private String number;
    private String currency;
    private double amount;
    private String url;
    private String creationDate;
}
