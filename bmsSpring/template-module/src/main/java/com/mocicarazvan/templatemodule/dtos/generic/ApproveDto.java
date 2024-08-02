package com.mocicarazvan.templatemodule.dtos.generic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
@Schema(description = "The default schema for an approved type entity")
public class ApproveDto extends TitleBodyUserDtoImages {
    @Schema(description = "The state of the approved status for the entity")
    private boolean approved = false;
}
