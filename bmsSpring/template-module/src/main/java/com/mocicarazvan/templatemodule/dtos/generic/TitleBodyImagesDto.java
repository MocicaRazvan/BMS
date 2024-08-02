package com.mocicarazvan.templatemodule.dtos.generic;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public abstract class TitleBodyImagesDto extends TitleBodyDto {

    @NotEmpty(message = "The images should not be empty.")
    private List<String> images;
}
