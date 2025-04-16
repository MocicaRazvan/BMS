package com.mocicarazvan.templatemodule.clients;

import com.mocicarazvan.templatemodule.dtos.files.GridIdsDto;
import com.mocicarazvan.templatemodule.dtos.files.MetadataDto;
import com.mocicarazvan.templatemodule.dtos.response.FileUploadResponse;
import com.mocicarazvan.templatemodule.exceptions.client.ThrowFallback;
import com.mocicarazvan.templatemodule.exceptions.common.ServiceCallFailedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class FileClient extends ClientBase {
    @Value("${file-service.url}")
    private String fileServiceUrl;

    public FileClient(String service, WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(service, webClientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
    }

    public Mono<FileUploadResponse> uploadFiles(Flux<FilePart> files, MetadataDto metadata) {
        return files.collectList().flatMap(fileParts -> {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            fileParts.forEach(filePart -> bodyBuilder.part("files", filePart)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .filename(filePart.filename()));
            bodyBuilder.part("metadata", metadata);


            return getClient()
                    .post()
                    .uri("/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                    .bodyToMono(FileUploadResponse.class)
                    .transform(this::applyResilience)
                    .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                    .onErrorResume(ThrowFallback.class, e ->
                    {
                        log.error("Error uploading files", e);
                        return Mono.error(new ServiceCallFailedException(
                                "Error uploading files to file-service", "file-service", "/upload"));
                    });
        });
    }

    public Mono<Void> deleteFiles(List<String> urls) {
//        GridIdsDto ids = new GridIdsDto(urls.stream()
//                .map(i -> i.substring(i.lastIndexOf('/') + 1))
//                .toList());

        return
                Flux.fromIterable(urls)
                        .map(i -> i.substring(i.lastIndexOf('/') + 1))
                        .collectList()
                        .map(GridIdsDto::new)
                        .flatMap(ids ->
                                getClient().method(HttpMethod.DELETE)
                                        .uri("/delete")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(ids)
                                        .retrieve()
                                        .onStatus(HttpStatusCode::isError, response -> ClientExceptionHandler.handleClientException(response, serviceUrl, service))
                                        .bodyToMono(Void.class)
                                        .transformDeferred(RetryOperator.of(retry))
                                        .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                                        .transformDeferred(RateLimiterOperator.of(rateLimiter))
                                        .onErrorResume(WebClientRequestException.class, ClientExceptionHandler::handleWebRequestException)
                                        .onErrorResume(ThrowFallback.class, e ->
                                        {
                                            log.error("Error deleting files", e);
                                            return Mono.error(new ServiceCallFailedException(
                                                    "Error deleting files from file-service", "file-service", "/delete"));
                                        })
                        );


    }

    @Override
    public WebClient getClient() {
        return webClientBuilder.baseUrl(fileServiceUrl + "/files")
                .build();
    }

    @PostConstruct
    public void init() {
        setServiceUrl(fileServiceUrl);
    }
}
