package com.mocicarazvan.orderservice.models;

import com.mocicarazvan.orderservice.enums.DietType;
import com.mocicarazvan.orderservice.enums.ObjectiveType;
import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("plan_order")
public class PlanOrder extends ManyToOneUser implements Cloneable {
    private double price;
    private DietType type;
    private ObjectiveType objective;
    private Long planId;
    private LocalDateTime planCreatedAt;
    private LocalDateTime planUpdatedAt;
    private Long orderId;
    private String title;

    @Override
    public PlanOrder clone() {
        PlanOrder clone = (PlanOrder) super.clone();
        clone.setPrice(price);
        clone.setType(type);
        clone.setObjective(objective);
        clone.setPlanId(planId);
        clone.setPlanCreatedAt(planCreatedAt);
        clone.setPlanUpdatedAt(planUpdatedAt);
        clone.setOrderId(orderId);
        return clone;
    }

}
