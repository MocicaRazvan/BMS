package com.mocicarazvan.templatemodule.dtos.files;


import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GridIdsDto {
    @NotEmpty(message = "GridFsIds cannot be empty")
    private List<String> gridFsIds;
}
