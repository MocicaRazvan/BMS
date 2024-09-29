package com.mocicarazvan.orderservice.models;

import com.mocicarazvan.templatemodule.models.IdGenerated;
import com.stripe.model.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("address")
public class CustomAddress extends IdGenerated {
    @Column("city")
    private String city;
    @Column("country")
    private String country;
    @Column("line1")
    private String line1;
    @Column("line2")
    private String line2;
    @Column("postal_code")
    private String postalCode;
    @Column("state")
    private String state;

    public static CustomAddress fromStripe(Address address) {
        return CustomAddress.builder()
                .city(address.getCity())
                .country(address.getCountry())
                .line1(address.getLine1())
                .line2(address.getLine2() != null ? address.getLine2() : "")
                .postalCode(address.getPostalCode())
                .state(address.getState())
                .build();
    }
}
