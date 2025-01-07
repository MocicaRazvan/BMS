package com.mocicarazvan.gatewayservice.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class NextJSJWE {
    private String email;
    private String picture;
    private String sub;
    private UserPayloadJWE user;
    private long iat;
    private long exp;
    private String jti;
}
