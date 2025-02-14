package com.mocicarazvan.gatewayservice.dtos.csrf;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CsrfValidationResponse {
    private boolean valid;

    public static CsrfValidationResponse valid() {
        return CsrfValidationResponse.builder().valid(true).build();
    }

    public static CsrfValidationResponse invalid() {
        return CsrfValidationResponse.builder().valid(false).build();
    }
}
