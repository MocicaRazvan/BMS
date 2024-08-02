package com.mocicarazvan.templatemodule.dtos.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {

    @NotNull(message = "The recipient email must not be null")
    @Email(message = "The recipient email must be a valid email")
    private String recipientEmail;

    @NotNull(message = "The subject must not be null")
    @NotEmpty(message = "The subject must not be empty")
    private String subject;

    @NotNull(message = "The content must not be null")
    @NotEmpty(message = "The content must not be empty")
    private String content;
}
