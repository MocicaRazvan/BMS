package com.mocicarazvan.userservice.dtos.otp;


import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OTPRequest {

    @NotEmpty(message = "Email should be not empty!")
    private String email;
}
