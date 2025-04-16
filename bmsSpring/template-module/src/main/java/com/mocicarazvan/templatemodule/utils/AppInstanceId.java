package com.mocicarazvan.templatemodule.utils;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AppInstanceId {

    public static final String APP_INSTANCE_ID = UUID.randomUUID().toString();
    public static final String APP_INSTANCE_ID_HEADER = "x-app-instance-id";

    public static Map<String, Object> getAppInstanceIdHeader() {
        return Map.of(APP_INSTANCE_ID_HEADER, APP_INSTANCE_ID);
    }

    public static Map.Entry<String, Object> getAppInstanceIdHeaderEntry() {
        return Map.entry(APP_INSTANCE_ID_HEADER, APP_INSTANCE_ID);
    }

    public static boolean isSameAppInstanceId(String appInstanceId) {
        return Objects.equals(APP_INSTANCE_ID, appInstanceId);
    }

}
