package com.mocicarazvan.nextstaticserver.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.function.Function;


public class EtagGenerator implements Function<Resource, String> {
    private final Logger logger = LoggerFactory.getLogger(EtagGenerator.class);

    @Override
    public String apply(Resource resource) {
        try {
            String name = resource.getFilename();
            if (name == null) {
                return null;
            }
            String base = name.substring(0, name.lastIndexOf('.'));
            if (base.isEmpty()) {
                return null;
            }
            long size = resource.contentLength();
            long mtime = resource.lastModified();
            String hexSize = Long.toHexString(size);
            String hexMtime = Long.toHexString(mtime);
            String sanitizedBase = HeaderSanitizer.sanitize(base
                    .substring(0, Math.min(base.length(), 128)));
            return "W/\"" + hexSize + "-" + hexMtime + sanitizedBase + "\"";
        } catch (Exception e) {
            logger.warn("Error generating ETag for resource: {} - {}", resource.getFilename(), e.getMessage());
            return null;
        }
    }
}
