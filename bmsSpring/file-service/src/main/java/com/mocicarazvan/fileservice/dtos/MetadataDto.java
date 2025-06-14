package com.mocicarazvan.fileservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mocicarazvan.fileservice.enums.FileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MetadataDto {

    @NotBlank(message = "Name cannot be blank")
    private String name;
    @NotNull(message = "File type cannot be null")
    private FileType fileType;
    private String clientId;

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

    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    @JsonProperty("clientId")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
