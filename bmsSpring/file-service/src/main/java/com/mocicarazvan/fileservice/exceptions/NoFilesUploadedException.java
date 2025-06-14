package com.mocicarazvan.fileservice.exceptions;

public class NoFilesUploadedException extends RuntimeException {
    public NoFilesUploadedException() {
        super("At least one file must be uploaded");
    }
}