package com.mocicarazvan.orderservice.dtos;


import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CustomAddressDto extends IdGenerateDto {
    private String city;

    private String country;

    private String line1;

    private String line2;

    private String postalCode;

    private String state;
}
