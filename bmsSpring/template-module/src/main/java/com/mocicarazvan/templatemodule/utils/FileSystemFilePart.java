package com.mocicarazvan.templatemodule.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class FileSystemFilePart implements FilePart {

    private final File file;
    private final String name;
    private final DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

    @Override
    public String filename() {
        return file.getName().replaceAll("\\.tmp$", "");
    }

    @Override
    public Mono<Void> transferTo(Path dest) {
        return DataBufferUtils.write(content(), dest)
                .then();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        try {
            String contentType = Files.probeContentType(file.toPath());
            headers.setContentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM);
        } catch (IOException e) {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }
        headers.setContentDispositionFormData(name, filename());
        return headers;
    }

    @Override
    public Flux<DataBuffer> content() {
        return DataBufferUtils.read(file.toPath(), dataBufferFactory, 1024);
    }

    @Override
    public Mono<Void> delete() {
        return Mono.fromRunnable(() -> {
            if (file.exists()) {
                log.info("Deleting file: {}", file.getName());
                file.delete();
            }
        });
    }
}