package com.mocicarazvan.fileservice.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomCustomMediaTypeTest {

    public static Stream<Arguments> namesWihTypes() {
        return Stream.of(
                Arguments.of("image.png", CustomMediaType.PNG),
                Arguments.of("photo.jpg", CustomMediaType.JPG),
                Arguments.of("picture.jpeg", CustomMediaType.JPEG),
                Arguments.of("video.mp4", CustomMediaType.MP4),
                Arguments.of("document.pdf", CustomMediaType.ALL),
                Arguments.of(null, CustomMediaType.ALL),
                Arguments.of("", CustomMediaType.ALL),
                Arguments.of("archive.tar.gz", CustomMediaType.ALL),
                Arguments.of("file.", CustomMediaType.ALL),
                Arguments.of("file", CustomMediaType.ALL),
                Arguments.of("IMAGE.PNG", CustomMediaType.PNG),
                Arguments.of("photo.JpG", CustomMediaType.JPG)
        );
    }

    @ParameterizedTest
    @MethodSource("namesWihTypes")
    void fromFileNameReturnsCorrectMediaType(String fileName, CustomMediaType expectedMediaType) {
        assertEquals(expectedMediaType, CustomMediaType.fromFileName(fileName));
    }

    @Test
    void fromFileNameReturnsAllForNullFileName() {
        assertEquals(CustomMediaType.ALL, CustomMediaType.fromFileName(null));
    }

    @Test
    void fromFileNameReturnsAllForEmptyFileName() {
        assertEquals(CustomMediaType.ALL, CustomMediaType.fromFileName(""));
    }

    @Test
    void fromFileNameReturnsAllForFileNameWithoutExtension() {
        assertEquals(CustomMediaType.ALL, CustomMediaType.fromFileName("file"));
    }

    @Test
    void fromValueReturnsCorrectMediaTypeForLowerCasePng() {
        assertEquals(CustomMediaType.PNG, CustomMediaType.fromValue("png"));
    }

    @Test
    void fromValueReturnsCorrectMediaTypeForUpperCaseJpg() {
        assertEquals(CustomMediaType.JPG, CustomMediaType.fromValue("JPG"));
    }

    @Test
    void fromValueReturnsCorrectMediaTypeForMixedCaseJpeg() {
        assertEquals(CustomMediaType.JPEG, CustomMediaType.fromValue("J.PeG"));
    }

    @Test
    void fromValueReturnsAllForUnknownValue() {
        assertEquals(CustomMediaType.ALL, CustomMediaType.fromValue("unknown"));
    }

    @Test
    void fromValueReturnsAllForNullValue() {
        assertEquals(CustomMediaType.ALL, CustomMediaType.fromValue(null));
    }

    @Test
    void fromValueReturnsAllForEmptyValue() {
        assertEquals(CustomMediaType.ALL, CustomMediaType.fromValue(""));
    }


    @Test
    void fromValueReturnsAllForValueWithSpaces() {
        assertEquals(CustomMediaType.ALL, CustomMediaType.fromValue(" mp4 "));
    }


}