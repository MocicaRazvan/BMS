package com.mocicarazvan.orderservice.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicesResponse {
    private List<CustomInvoiceDto> invoices;
    private boolean nextPageHasItems;
}
