package com.mocicarazvan.cartservice.exceptions;

import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundBase;
import lombok.Getter;

import java.util.List;

@Getter
public class PlansNotInCart extends NotFoundBase {
    private List<Long> givenIds;
    private List<Long> allIds;

    public PlansNotInCart(List<Long> givenIds, List<Long> allIds) {
        super(String.format("The following plans are not in the cart: %s , the cart contains: %s", givenIds, allIds));
    }
}
