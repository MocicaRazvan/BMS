package com.mocicarazvan.nextstaticserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.time.Duration;
import java.util.logging.Logger;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.static.path}")
    private String staticPath;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/_next/static/**")
                .addResourceLocations(staticPath)
                .setCacheControl(CacheControl
                        .maxAge(Duration.ofDays(365))
                        .cachePublic()
                        .immutable()
                )
                .setEtagGenerator((resource) -> {
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
                        return "W/\"" + hexSize + "-" + hexMtime + base
                                .substring(0, Math.min(base.length(), 128))
                                + "\"";
                    } catch (Exception e) {
                        logger.warning("Error generating ETag for resource: " + resource.getFilename() + " - " + e.getMessage());
                        return null;
                    }
                })
                .setUseLastModified(true)
                .setOptimizeLocations(true)
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new PathResourceResolver());
    }
}

