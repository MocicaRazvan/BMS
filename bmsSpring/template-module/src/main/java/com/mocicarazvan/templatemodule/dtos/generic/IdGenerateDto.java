package com.mocicarazvan.templatemodule.dtos.generic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public abstract class IdGenerateDto {

    private Long id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
