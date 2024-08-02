package com.mocicarazvan.templatemodule.dtos.generic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public abstract class TitleBodyDto {
    @NotBlank(message = "The body should not be blank.")
    @Schema(description = "The entity's body")
    private String body;

    @NotBlank(message = "The title should not be blank.")
    @Schema(description = "The entity's title")
    private String title;

}
