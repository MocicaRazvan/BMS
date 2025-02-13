package com.mocicarazvan.cartservice.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartDeletedResponse {
    private boolean deleted;

    public static CartDeletedResponse cartDeletedResponseSuccess() {
        return CartDeletedResponse.builder().deleted(true).build();
    }
}
