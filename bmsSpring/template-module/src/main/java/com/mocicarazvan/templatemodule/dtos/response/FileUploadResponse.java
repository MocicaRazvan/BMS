package com.mocicarazvan.templatemodule.dtos.response;


import com.mocicarazvan.templatemodule.enums.FileType;
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
