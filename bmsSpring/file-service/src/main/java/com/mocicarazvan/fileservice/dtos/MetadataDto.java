package com.mocicarazvan.fileservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mocicarazvan.fileservice.enums.FileType;

public class MetadataDto {

    private String name;
    private FileType fileType;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("fileType")
    public FileType getFileType() {
        return fileType;
    }

    @JsonProperty("fileType")
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }
}
