package com.mocicarazvan.fileservice.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FileNotFound extends RuntimeException {
    private String gridFsId;

    public FileNotFound(String gridFsId) {
        super("File not found with gridFsId: " + gridFsId);
    }

}
