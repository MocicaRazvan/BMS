package com.mocicarazvan.fileservice.utils;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheHeaderUtilsTest {

    long timestamp = 1672531200000L;


    @Test
    void buildETagGeneratesCorrectETagWithAllParameters() {
        String etag = CacheHeaderUtils.buildETag("grid123", 1920, 1080, 0.8, true, timestamp);
        assertEquals("\"grid123-w1920-h1080-q0.8-webp-1672531200000\"", etag);
    }

    @Test
    void buildETagGeneratesCorrectETagWithNullWidthAndHeight() {
        String etag = CacheHeaderUtils.buildETag("grid123", null, null, 0.8, null, timestamp);
        assertEquals("\"grid123-q0.8-1672531200000\"", etag);
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

    @Test
    void buildETagOmitsWebpWhenWebpDisabled() {
        String etag = CacheHeaderUtils.buildETag("grid123", 800, 600, 0.5, false, timestamp);
        assertEquals("\"grid123-w800-h600-q0.5-1672531200000\"", etag);
    }

    @Test
    void buildETagIncludesWidthAndHeightWhenQualityNull() {
        String etag = CacheHeaderUtils.buildETag("grid123", 1024, 768, null, null, timestamp);
        assertEquals("\"grid123-w1024-h768-1672531200000\"", etag);
    }

    @Test
    void clearCacheHeadersDoesNotRemoveWhenResponseCommitted() {
        MockServerHttpResponse raw = new MockServerHttpResponse();
        ServerHttpResponse response = spy(raw);
        when(response.isCommitted()).thenReturn(true);
        raw.getHeaders().setETag("\"etagX\"");
        raw.getHeaders().setLastModified(timestamp);
        raw.getHeaders().setCacheControl("no-cache");

        CacheHeaderUtils.clearCacheHeaders(response);

        assertEquals("\"etagX\"", raw.getHeaders().getETag());
        assertEquals(timestamp, raw.getHeaders().getLastModified());
        assertEquals("no-cache", raw.getHeaders().getCacheControl());
    }

    @Test
    void etagEqualsIgnoresSurroundingQuotes() {
        assertTrue(CacheHeaderUtils.etagEquals("etagX", "\"etagX\""));
        assertTrue(CacheHeaderUtils.etagEquals("\"etagX\"", "etagX"));
        assertTrue(CacheHeaderUtils.etagEquals("\"etagX\"", "\"etagX\""));
    }

    @Test
    void etagEqualsReturnsFalseForDifferentContents() {
        assertFalse(CacheHeaderUtils.etagEquals("etagA", "\"etagB\""));
    }

    @Test
    void buildETagHandlesEmptyGridId() {
        String etag = CacheHeaderUtils.buildETag("", 800, 600, 0.5, true, timestamp);
        assertNotEquals("\"-w800-h600-q0.5-webp-1672531200000\"", etag);
    }

    @Test
    void buildETagHandlesNullGridId() {
        String etag = CacheHeaderUtils.buildETag(null, 800, 600, 0.5, true, timestamp);
        assertNotEquals("\"null-w800-h600-q0.5-webp-1672531200000\"", etag);
    }

    @Test
    void buildETagHandlesNegativeWidthAndHeight() {
        String etag = CacheHeaderUtils.buildETag("grid123", -800, -600, 0.5, true, timestamp);
        assertEquals("\"grid123-w-800-h-600-q0.5-webp-1672531200000\"", etag);
    }

    @Test
    void buildETagHandlesZeroQuality() {
        String etag = CacheHeaderUtils.buildETag("grid123", 800, 600, 0.0, true, timestamp);
        assertEquals("\"grid123-w800-h600-q0.0-webp-1672531200000\"", etag);
    }

    @Test
    void buildETagHandlesNullTimestamp() {
        String etag = CacheHeaderUtils.buildETag("grid123", 800, 600, 0.5, true, 0L);
        assertEquals("\"grid123-w800-h600-q0.5-webp-0\"", etag);
    }


    @Test
    void setCachingHeadersHandlesNullFileType() {
        ServerHttpResponse response = new MockServerHttpResponse();
        GridFSFile gridFSFile = mock(GridFSFile.class);
        when(gridFSFile.getUploadDate()).thenReturn(new Date(timestamp));

        CacheHeaderUtils.setCachingHeaders(response, gridFSFile, null);

        assertEquals(timestamp, response.getHeaders().getLastModified());
        assertEquals("max-age=604800, no-transform, public, stale-if-error=86400, stale-while-revalidate=43200, immutable", response.getHeaders().getCacheControl());
    }

    @Test
    void clearCacheHeadersHandlesEmptyHeaders() {
        ServerHttpResponse response = new MockServerHttpResponse();

        CacheHeaderUtils.clearCacheHeaders(response);

        assertNull(response.getHeaders().getETag());
        assertEquals(-1L, response.getHeaders().getLastModified());
        assertNull(response.getHeaders().getCacheControl());
    }

    @Test
    void buildVideoEtag() {
        String etag = CacheHeaderUtils.buildETag("video123", timestamp);
        assertEquals("\"video123-1672531200000\"", etag);
    }


}