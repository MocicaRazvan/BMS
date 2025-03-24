package com.mocicarazvan.templatemodule.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class IdsDto {
    private Long id;
    private List<Long> referenceIds;
}
