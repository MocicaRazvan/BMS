package com.mocicarazvan.fileservice.enums;


import lombok.Getter;

@Getter
public enum CustomMediaType {

    PNG("png"),
    JPG("jpg"),
    JPEG("jpeg"),
    MP4("mp4"),
    ALL("**");

    private final String value;

    CustomMediaType(String value) {
        this.value = value;
    }

    public static CustomMediaType fromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return ALL;
        }

        return CustomMediaType.fromValue(fileName.substring(fileName.lastIndexOf('.') + 1));
    }

    public static CustomMediaType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }

        return switch (value
                .replace(".", "")
                .toLowerCase()) {
            case "png" -> PNG;
            case "jpg" -> JPG;
            case "jpeg" -> JPEG;
            case "mp4" -> MP4;
            default -> ALL;
        };
    }

    public String getContentTypeValueMedia() {
        return switch (this) {
            case PNG -> "png";
            case JPG, JPEG -> "jpeg";
            case MP4 -> "mp4";
            case ALL -> "**";
        };
    }


}
