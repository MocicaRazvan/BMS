package com.mocicarazvan.fileservice.utils;

import com.mocicarazvan.fileservice.enums.FileType;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

        GridFSFile gridFSFile = mock(GridFSFile.class);
        when(gridFSFile.getUploadDate()).thenReturn(new Date(timestamp));

        CacheHeaderUtils.setCachingHeaders(response, gridFSFile, "grid123", FileType.VIDEO);

        HttpHeaders headers = response.getHeaders();
        assertEquals("\"grid123-1672531200000\"", headers.getETag());
        assertEquals(timestamp, headers.getLastModified());
        assertEquals(CacheHeaderUtils.VIDEO_CACHE_CONTROL.getHeaderValue(), headers.getCacheControl());
    }

    @Test
    void setCachingHeadersSetsCorrectHeadersForImageFileType() {
        ServerHttpResponse response = new MockServerHttpResponse();
        GridFSFile gridFSFile = mock(GridFSFile.class);
        when(gridFSFile.getUploadDate()).thenReturn(new Date(timestamp));

        CacheHeaderUtils.setCachingHeaders(response, gridFSFile, "grid123", FileType.IMAGE);

        HttpHeaders headers = response.getHeaders();
        assertEquals("\"grid123-1672531200000\"", headers.getETag());
        assertEquals(timestamp, headers.getLastModified());
        assertEquals(CacheHeaderUtils.IMAGE_CACHE_CONTROL.getHeaderValue(), headers.getCacheControl());
    }

    @Test
    void setCachingHeadersSetCorrectHeadersForRangesNotEmpty() {
        ServerHttpResponse response = new MockServerHttpResponse();
        List<HttpRange> ranges = HttpRange.parseRanges("bytes=0-100");
        GridFSFile gridFSFile = mock(GridFSFile.class);

        CacheHeaderUtils.setCachingHeaders(response, gridFSFile, "grid123", FileType.VIDEO, ranges);

        HttpHeaders headers = response.getHeaders();
        assertEquals(CacheHeaderUtils.VIDEO_CACHE_RANGE_CONTROL.getHeaderValue(), headers.getCacheControl());
    }

    @Test
    void setCachingHeadersResponseAlreadyCommited() {
        ServerHttpResponse response = spy(new MockServerHttpResponse());
        when(response.isCommitted()).thenReturn(true);
        List<HttpRange> ranges = HttpRange.parseRanges("bytes=0-100");


        GridFSFile gridFSFile = mock(GridFSFile.class);
        CacheHeaderUtils.setCachingHeaders(response, gridFSFile, "grid123", FileType.VIDEO, ranges);

        assertNull(response.getHeaders().getETag());
        assertEquals(-1L, response.getHeaders().getLastModified());
        assertNull(response.getHeaders().getCacheControl());
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

    @Test
    void clearCacheHeadersClearsAllHeaders() {
        ServerHttpResponse response = new MockServerHttpResponse();
        response.getHeaders().setETag("\"etag123\"");
        response.getHeaders().setLastModified(1672531200000L);
        response.getHeaders().setCacheControl("no-cache");

        CacheHeaderUtils.clearCacheHeaders(response);

        assertNull(response.getHeaders().getETag());
        assertEquals(-1L, response.getHeaders().getLastModified());
        assertNull(response.getHeaders().getCacheControl());
    }
}