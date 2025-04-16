package com.mocicarazvan.fileservice.utils;

import com.mocicarazvan.fileservice.enums.FileType;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

public class CacheHeaderUtils {

    public static final String IMAGE_CACHE_CONTROL = "public, max-age=604800, immutable";
    public static final String VIDEO_CACHE_CONTROL = "public, max-age=86400, immutable";

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
