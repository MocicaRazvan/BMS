package com.mocicarazvan.fileservice.dtos;

import com.mocicarazvan.fileservice.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressUpdateDto {
    private Long index;
    private String message;
    private FileType fileType;
}
