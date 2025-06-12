package com.mocicarazvan.nextstaticserver.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EtagGeneratorTest {

    @Mock
    private Resource resource;

    @Test
    void generatesWeakEtagForValidResource() throws Exception {
        when(resource.getFilename()).thenReturn("index.html");
        when(resource.contentLength()).thenReturn(1024L);
        when(resource.lastModified()).thenReturn(2048L);

        String result = new EtagGenerator().apply(resource);

        assertEquals("W/\"400-800index\"", result);
    }

    @Test
    void returnsNullWhenFilenameIsNull() {
        when(resource.getFilename()).thenReturn(null);

        String result = new EtagGenerator().apply(resource);

        assertNull(result);
    }

    @Test
    void returnsNullWhenNameHasNoExtension() throws Exception {
        when(resource.getFilename()).thenReturn("filewithnodot");


        String result = new EtagGenerator().apply(resource);

        assertNull(result);
    }

    @Test
    void returnsNullWhenBaseIsEmpty() throws Exception {
        when(resource.getFilename()).thenReturn(".hiddenext");


        String result = new EtagGenerator().apply(resource);

        assertNull(result);
    }

    @Test
    void returnsNullWhenContentLengthThrows() throws Exception {
        when(resource.getFilename()).thenReturn("error.txt");
        when(resource.contentLength()).thenThrow(new IOException("fail"));


        String result = new EtagGenerator().apply(resource);

        assertNull(result);
    }
}