package com.mocicarazvan.templatemodule.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "The user request object")
public class UserBody {

    @NotBlank(message = "The first name should not be empty.")
    @Schema(description = "User's first name")
    private String firstName;

    @NotBlank(message = "The last name should not be empty.")
    @Schema(description = "User's last name")
    private String lastName;


//    private String image;


}
