package com.mocicarazvan.userservice.dtos.auth.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "The object provided by the user to register")
public class RegisterRequest extends LoginRequest {
    @NotBlank(message = "The first name should not be empty.")
    @Schema(description = "User's first name")
    private String firstName;
    @NotBlank(message = "The last name should not be empty.")
    @Schema(description = "User's last name")
    private String lastName;


    private String image;
}
