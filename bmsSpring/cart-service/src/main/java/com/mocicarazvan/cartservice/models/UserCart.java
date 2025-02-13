package com.mocicarazvan.cartservice.models;

import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("user_cart")
public class UserCart extends ManyToOneUser implements Cloneable {
    private List<Long> planIds = new ArrayList<>();

    @Override
    public UserCart clone() {
        UserCart userCart = (UserCart) super.clone();
        userCart.setPlanIds(List.copyOf(this.planIds));
        return userCart;
    }
}
