package com.mocicarazvan.fileservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadErrorResponse {
    private String message;
    private String timestamp;
    private String error;
    private String path;
    private int status;
    private String gridFsId;
}
