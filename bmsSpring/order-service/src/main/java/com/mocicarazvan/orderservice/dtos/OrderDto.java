package com.mocicarazvan.orderservice.dtos;

import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderDto extends WithUserDto {
    private Long addressId;
    private List<Long> planIds;
    private double total;
    private String stripeInvoiceId;

}
