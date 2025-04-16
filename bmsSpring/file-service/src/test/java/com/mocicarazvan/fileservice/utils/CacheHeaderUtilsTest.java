package com.mocicarazvan.fileservice.utils;

import com.mocicarazvan.fileservice.enums.FileType;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CacheHeaderUtilsTest {

    long timestamp = 1672531200000L;


    @Test
    void buildETagGeneratesCorrectETagWithAllParameters() {
        String etag = CacheHeaderUtils.buildETag("grid123", 1920, 1080, 0.8, timestamp);
        assertEquals("\"grid123-w1920-h1080-q0.8-1672531200000\"", etag);
    }

    @Test
    void buildETagGeneratesCorrectETagWithNullWidthAndHeight() {
        String etag = CacheHeaderUtils.buildETag("grid123", null, null, 0.8, timestamp);
        assertEquals("\"grid123-q0.8-1672531200000\"", etag);
    }

    @Test
    void setCachingHeadersSetsCorrectHeadersForVideoFileType() {
        ServerHttpResponse response = new MockServerHttpResponse();
        ServerHttpRequest request = mock(ServerHttpRequest.class);

        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(request.getHeaders().getFirst("X-Bypass-Cache")).thenReturn(null);
        GridFSFile gridFSFile = mock(GridFSFile.class);
        when(gridFSFile.getUploadDate()).thenReturn(new Date(timestamp));

        CacheHeaderUtils.setCachingHeaders(response, request, gridFSFile, "grid123", FileType.VIDEO);

        HttpHeaders headers = response.getHeaders();
        assertEquals("\"grid123-1672531200000\"", headers.getETag());
        assertEquals(timestamp, headers.getLastModified());
        assertEquals(CacheHeaderUtils.VIDEO_CACHE_CONTROL.getHeaderValue(), headers.getCacheControl());
    }

    @Test
    void setCachingHeadersSetsCorrectHeadersForImageFileType() {
        ServerHttpResponse response = new MockServerHttpResponse();
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(request.getHeaders().getFirst("X-Bypass-Cache")).thenReturn(null);
        GridFSFile gridFSFile = mock(GridFSFile.class);
        when(gridFSFile.getUploadDate()).thenReturn(new Date(timestamp));

        CacheHeaderUtils.setCachingHeaders(response, request, gridFSFile, "grid123", FileType.IMAGE);

        HttpHeaders headers = response.getHeaders();
        assertEquals("\"grid123-1672531200000\"", headers.getETag());
        assertEquals(timestamp, headers.getLastModified());
        assertEquals(CacheHeaderUtils.IMAGE_CACHE_CONTROL.getHeaderValue(), headers.getCacheControl());
    }

    @Test
    void etagEqualsReturnsTrueForMatchingETags() {
        assertTrue(CacheHeaderUtils.etagEquals("\"etag123\"", "\"etag123\""));
    }

    @Test
    void etagEqualsReturnsFalseForNonMatchingETags() {
        assertFalse(CacheHeaderUtils.etagEquals("\"etag123\"", "\"etag456\""));
    }

    @Test
    void etagEqualsReturnsFalseWhenOneETagIsNull() {
        assertFalse(CacheHeaderUtils.etagEquals(null, "\"etag123\""));
    }

    @Test
    void etagEqualsReturnsFalseWhenBothETagsAreNull() {
        assertFalse(CacheHeaderUtils.etagEquals(null, null));
    }
}