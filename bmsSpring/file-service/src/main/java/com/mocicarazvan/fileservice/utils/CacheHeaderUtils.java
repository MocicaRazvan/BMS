package com.mocicarazvan.fileservice.utils;

import com.mocicarazvan.fileservice.enums.FileType;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.http.CacheControl;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import java.time.Duration;

public class CacheHeaderUtils {

    public static final CacheControl IMAGE_CACHE_CONTROL = CacheControl
            .maxAge(Duration.ofDays(7))
            .staleWhileRevalidate(Duration.ofHours(12))
            .staleIfError(Duration.ofDays(1))
            .cachePublic()
            .immutable()
            .noTransform();

    public static final CacheControl VIDEO_CACHE_CONTROL = CacheControl
            .maxAge(Duration.ofDays(3))
            .staleWhileRevalidate(Duration.ofHours(12))
            .staleIfError(Duration.ofDays(1))
            .cachePublic()
            .immutable()
            .noTransform();


    public static String buildETag(String gridId, Integer width, Integer height, Double quality, long timestamp) {
        StringBuilder builder = new StringBuilder("\"" + gridId);

        if (width != null) {
            builder.append("-w").append(width);
        }
        if (height != null) {
            builder.append("-h").append(height);
        }
        if (quality != null) {
            builder.append("-q").append(quality);
        }

        builder.append("-").append(timestamp).append("\"");

        return builder.toString();
    }

    public static void setCachingHeaders(ServerHttpResponse response, ServerHttpRequest request, GridFSFile gridFSFile, String gridId, FileType fileType) {

//        String bypass = request.getHeaders().getFirst("X-Bypass-Cache");
//        if (bypass != null && (bypass.equals("true") || bypass.equals("1"))) {
//            response.getHeaders().setCacheControl("no-store");
//            return;
//        }
        String etag = "\"" + gridId + "-" + gridFSFile.getUploadDate().getTime() + "\"";
        response.getHeaders().setETag(etag);
        response.getHeaders().setLastModified(gridFSFile.getUploadDate().getTime());

        if (fileType == FileType.VIDEO) {
            response.getHeaders().setCacheControl(VIDEO_CACHE_CONTROL);
        } else {
            response.getHeaders().setCacheControl(IMAGE_CACHE_CONTROL);
        }
    }

    public static void clearCacheHeaders(ServerHttpResponse response) {
        if (response.isCommitted()) {
            return;
        }
        response.getHeaders().remove("Cache-Control");
        response.getHeaders().remove("ETag");
        response.getHeaders().remove("Last-Modified");
    }


    public static boolean etagEquals(String a, String b) {
        if (a == null || b == null) return false;
        String cleanA = a.replace("\"", "");
        String cleanB = b.replace("\"", "");

        return cleanA.equals(cleanB);
    }
}
