package com.mocicarazvan.templatemodule.utils;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AppInstanceIdTest {
    @Test
    void appInstanceIdHeaderContainsCorrectKeyAndValue() {
        Map<String, Object> header = AppInstanceId.getAppInstanceIdHeader();
        assertEquals(1, header.size());
        assertTrue(header.containsKey(AppInstanceId.APP_INSTANCE_ID_HEADER));
        assertEquals(AppInstanceId.APP_INSTANCE_ID, header.get(AppInstanceId.APP_INSTANCE_ID_HEADER));
    }

    @Test
    void appInstanceIdHeaderEntryContainsCorrectKeyAndValue() {
        Map.Entry<String, Object> headerEntry = AppInstanceId.getAppInstanceIdHeaderEntry();
        assertEquals(AppInstanceId.APP_INSTANCE_ID_HEADER, headerEntry.getKey());
        assertEquals(AppInstanceId.APP_INSTANCE_ID, headerEntry.getValue());
    }

    @Test
    void isSameAppInstanceIdReturnsTrueForMatchingId() {
        assertTrue(AppInstanceId.isSameAppInstanceId(AppInstanceId.APP_INSTANCE_ID));
    }

    @Test
    void isSameAppInstanceIdReturnsFalseForNonMatchingId() {
        assertFalse(AppInstanceId.isSameAppInstanceId(UUID.randomUUID().toString()));
    }

    @Test
    void isSameAppInstanceIdReturnsFalseForNullId() {
        assertFalse(AppInstanceId.isSameAppInstanceId(null));
    }
}