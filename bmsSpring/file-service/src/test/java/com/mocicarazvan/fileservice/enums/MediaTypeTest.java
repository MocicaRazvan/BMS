package com.mocicarazvan.fileservice.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MediaTypeTest {

    public static Stream<Arguments> namesWihTypes() {
        return Stream.of(
                Arguments.of("image.png", MediaType.PNG),
                Arguments.of("photo.jpg", MediaType.JPG),
                Arguments.of("picture.jpeg", MediaType.JPEG),
                Arguments.of("video.mp4", MediaType.MP4),
                Arguments.of("document.pdf", MediaType.ALL),
                Arguments.of(null, MediaType.ALL),
                Arguments.of("", MediaType.ALL),
                Arguments.of("archive.tar.gz", MediaType.ALL),
                Arguments.of("file.", MediaType.ALL),
                Arguments.of("file", MediaType.ALL),
                Arguments.of("IMAGE.PNG", MediaType.PNG),
                Arguments.of("photo.JpG", MediaType.JPG)
        );
    }

    @ParameterizedTest
    @MethodSource("namesWihTypes")
    void fromFileNameReturnsCorrectMediaType(String fileName, MediaType expectedMediaType) {
        assertEquals(expectedMediaType, MediaType.fromFileName(fileName));
    }

    @Test
    void fromFileNameReturnsAllForNullFileName() {
        assertEquals(MediaType.ALL, MediaType.fromFileName(null));
    }

    @Test
    void fromFileNameReturnsAllForEmptyFileName() {
        assertEquals(MediaType.ALL, MediaType.fromFileName(""));
    }

    @Test
    void fromFileNameReturnsAllForFileNameWithoutExtension() {
        assertEquals(MediaType.ALL, MediaType.fromFileName("file"));
    }

    @Test
    void fromValueReturnsCorrectMediaTypeForLowerCasePng() {
        assertEquals(MediaType.PNG, MediaType.fromValue("png"));
    }

    @Test
    void fromValueReturnsCorrectMediaTypeForUpperCaseJpg() {
        assertEquals(MediaType.JPG, MediaType.fromValue("JPG"));
    }

    @Test
    void fromValueReturnsCorrectMediaTypeForMixedCaseJpeg() {
        assertEquals(MediaType.JPEG, MediaType.fromValue("J.PeG"));
    }

    @Test
    void fromValueReturnsAllForUnknownValue() {
        assertEquals(MediaType.ALL, MediaType.fromValue("unknown"));
    }

    @Test
    void fromValueReturnsAllForNullValue() {
        assertEquals(MediaType.ALL, MediaType.fromValue(null));
    }

    @Test
    void fromValueReturnsAllForEmptyValue() {
        assertEquals(MediaType.ALL, MediaType.fromValue(""));
    }


    @Test
    void fromValueReturnsAllForValueWithSpaces() {
        assertEquals(MediaType.ALL, MediaType.fromValue(" mp4 "));
    }


}