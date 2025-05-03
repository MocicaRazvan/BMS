package com.mocicarazvan.templatemodule.clients;

import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.utils.FileSystemFilePart;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
public class DownloadFileClient {
    private final WebClient webClient;
    private final int parallelism;
    private final Executor executor;

    public DownloadFileClient(int parallelism, Executor executor) {
        HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> {
                    try {
                        sslContextSpec.sslContext(SslContextBuilder.forClient()
                                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                .build());
                    } catch (SSLException e) {
                        throw new RuntimeException("Failed to create insecure SSL context", e);
                    }
                });
        this.parallelism = parallelism;
        this.executor = executor;
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(useMaxMemory())
                .build();
    }

    public Flux<FilePart> downloadImages(List<String> urls, FileType fileType) {
        return Flux.fromIterable(urls)
                .publishOn(Schedulers.fromExecutor(executor))
                .flatMap(url ->
                                webClient
                                        .get()
                                        .uri(url)
                                        .exchangeToMono(response -> {
                                            String extension;
                                            if (fileType.equals(FileType.VIDEO)) {
                                                extension = ".mp4";
                                            } else {

                                                String contentType = response.headers().contentType()
                                                        .map(MediaType::toString)
                                                        .orElse("null");
                                                extension = getExtensionFromContentType(contentType);
                                            }
                                            Path tempFile;
                                            try {
                                                tempFile = Files.createTempFile("fileGenerated" + System.currentTimeMillis(), extension);
                                            } catch (IOException e) {
                                                return Mono.error(e);
                                            }

                                            Path finalTempFile = tempFile;
                                            return response.bodyToFlux(DataBuffer.class)
                                                    .transform(dataBufferFlux -> DataBufferUtils.write(dataBufferFlux, finalTempFile))
                                                    .then(
                                                            Mono.fromCallable(
                                                                    () -> {
                                                                        File file = finalTempFile.toFile();
                                                                        file.deleteOnExit();
                                                                        return new FileSystemFilePart(file, "files");
                                                                    }
                                                            )
                                                    );
                                        })
                                        .transformDeferred(RetryOperator.of(Retry.ofDefaults("downloadImages"))),
                        parallelism
                );
    }


    private static ExchangeStrategies useMaxMemory() {
        long totalMemory = Runtime.getRuntime().maxMemory() / 2;

        return ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize((int) totalMemory)
                )
                .build();
    }

    public static String getExtensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpeg";
            case "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "video/mp4" -> ".mp4";
            default -> ".tmp";
        };
    }
}
