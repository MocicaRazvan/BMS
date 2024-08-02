package com.mocicarazvan.orderservice.models;

import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("custom_order")
public class Order extends ManyToOneUser {
    @Column("address_id")
    private Long addressId;
    @Column("plan_ids")
    private List<Long> planIds;
    @Column("total")
    private double total;

    @Column("stripe_invoice_id")
    private String stripeInvoiceId;
}
