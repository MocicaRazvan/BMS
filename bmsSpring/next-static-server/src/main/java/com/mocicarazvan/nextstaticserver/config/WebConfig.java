package com.mocicarazvan.nextstaticserver.config;

import com.mocicarazvan.nextstaticserver.utils.EtagGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.time.Duration;

@Configuration

public class WebConfig implements WebMvcConfigurer {
    @Value("${app.static.path}")
    private String staticPath;

    private final EtagGenerator etagGenerator;

    public WebConfig() {
        this.etagGenerator = new EtagGenerator();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/_next/static/**")
                .addResourceLocations(staticPath)
                .setCacheControl(CacheControl
                        .maxAge(Duration.ofDays(365))
                        .cachePublic()
                        .immutable()
                )
                .setEtagGenerator(etagGenerator)
                .setUseLastModified(true)
                .setOptimizeLocations(true)
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new PathResourceResolver());
    }
}

