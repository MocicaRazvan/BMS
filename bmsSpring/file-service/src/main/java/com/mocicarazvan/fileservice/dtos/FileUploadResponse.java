package com.mocicarazvan.fileservice.dtos;


import com.mocicarazvan.fileservice.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponse {
    private List<String> files;
    private FileType fileType;
}
