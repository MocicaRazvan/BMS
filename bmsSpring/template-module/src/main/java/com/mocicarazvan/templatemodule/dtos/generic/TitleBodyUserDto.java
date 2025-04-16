package com.mocicarazvan.templatemodule.dtos.generic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public abstract class TitleBodyUserDto extends WithUserDto {
    @NotBlank(message = "The body should not be blank.")
    @Schema(description = "The entity's body")
    private String body;

    @NotBlank(message = "The title should not be blank.")
    @Schema(description = "The entity's title")
    private String title;

    @Schema(description = "The user ids that liked the entity")
    @Builder.Default
    private List<Long> userDislikes = new ArrayList<>();

    @Schema(description = "The user ids that disliked the entity")
    @Builder.Default
    private List<Long> userLikes = new ArrayList<>();
}
