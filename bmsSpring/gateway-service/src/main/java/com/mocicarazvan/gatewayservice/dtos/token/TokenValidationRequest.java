package com.mocicarazvan.gatewayservice.dtos.token;

import com.mocicarazvan.gatewayservice.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenValidationRequest {

    @NotNull
    private String token;


    @NotNull
    private Role minRoleRequired;
}
