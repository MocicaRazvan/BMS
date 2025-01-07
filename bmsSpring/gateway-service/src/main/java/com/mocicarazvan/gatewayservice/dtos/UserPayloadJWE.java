package com.mocicarazvan.gatewayservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserPayloadJWE {
    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String token;
    private String role;
    private String image;
    private String provider;
    private boolean emailVerified;
}
