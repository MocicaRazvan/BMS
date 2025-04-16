package com.mocicarazvan.websocketservice.utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class ChunkRequestTest {

    @Test
    void shouldCreateChunkRequestWithValidInputsAndSort() {
        Sort sort = Sort.by("property");
        ChunkRequest chunkRequest = new ChunkRequest(10, 5, sort);
        assertEquals(10, chunkRequest.getOffset());
        assertEquals(5, chunkRequest.getPageSize());
        assertEquals(sort, chunkRequest.getSort());
        assertEquals(0, chunkRequest.getPageNumber());
    }

    @Test
    void shouldCreateChunkRequestWithValidInputsWithoutSort() {
        ChunkRequest chunkRequest = new ChunkRequest(0, 10);
        assertEquals(0, chunkRequest.getOffset());
        assertEquals(10, chunkRequest.getPageSize());
        assertNull(chunkRequest.getSort());
    }

    @Test
    void shouldThrowExceptionForNegativeOffset() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new ChunkRequest(-1, 10));
        assertEquals("Offset must not be less than zero!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForNegativeLimit() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new ChunkRequest(0, -1));
        assertEquals("Limit must not be less than zero!", exception.getMessage());
    }

    @Test
    void shouldReturnSameInstanceForFirstAndPreviousOrFirst() {
        ChunkRequest chunkRequest = new ChunkRequest(0, 5);
        assertSame(chunkRequest, chunkRequest.first());
        assertSame(chunkRequest, chunkRequest.previousOrFirst());
    }

    @Test
    void shouldReturnNullForNotImplementedMethods() {
        ChunkRequest chunkRequest = new ChunkRequest(0, 5);
        assertNull(chunkRequest.next());
        assertNull(chunkRequest.withPage(1));
    }
}