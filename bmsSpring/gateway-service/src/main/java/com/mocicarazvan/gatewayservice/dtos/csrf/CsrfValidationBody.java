package com.mocicarazvan.gatewayservice.dtos.csrf;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CsrfValidationBody {
    @NotBlank(message = "CSRF token is required")
    private String csrf;
}
