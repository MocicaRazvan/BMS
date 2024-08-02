package com.mocicarazvan.userservice.dtos.auth.requests;


import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CallbackBody {
    @NotEmpty(message = "code is required")
    private String code;

    private String state;
}
