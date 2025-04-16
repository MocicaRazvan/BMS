package com.mocicarazvan.templatemodule.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.dtos.files.GridIdsDto;
import com.mocicarazvan.templatemodule.dtos.files.MetadataDto;
import com.mocicarazvan.templatemodule.dtos.response.FileUploadResponse;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.exceptions.common.ServiceCallFailedException;
import com.mocicarazvan.templatemodule.utils.FileSystemFilePart;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(MockitoExtension.class)
class FileClientTest {

    private MockWebServer mockWebServer;


    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private FileClient fileClient;

//    @Mock
//    private FilePart filePart;

    private final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    private final RetryRegistry retryRegistry = RetryRegistry.of(
            RetryConfig.custom()
                    .maxAttempts(3)
                    .waitDuration(Duration.ofMillis(10))
                    .build());
    private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/api").toString();

        WebClient.Builder clientBuilder = WebClient.builder();

        fileClient = new FileClient("test-service", clientBuilder, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
        ReflectionTestUtils.setField(fileClient, "fileServiceUrl", baseUrl);
        fileClient.setServiceUrl(baseUrl);
        fileClient.init();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @SneakyThrows
    void uploadFiles_success() {
        File tempFile = File.createTempFile("test-image", ".png");
        byte[] randomBytes = new byte[1024];
        new Random().nextBytes(randomBytes);
        Files.write(tempFile.toPath(), randomBytes);
        FilePart filePart = new FileSystemFilePart(tempFile, "files");
        Flux<FilePart> fileParts = Flux.just(filePart);
        MetadataDto metadataDto = new MetadataDto("img.png", FileType.IMAGE, "1");
        FileUploadResponse expected = new FileUploadResponse(List.of("https://im51.go.ro/files/123"), FileType.IMAGE);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(expected))
        );

        StepVerifier.create(fileClient.uploadFiles(fileParts, metadataDto))
                .expectNext(expected)
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/files/upload", recordedRequest.getPath());
        assertEquals("*/*", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());

        filePart.delete().block();
    }

    @Test
    @SneakyThrows
    void uploadFiles_error() {
        File tempFile = File.createTempFile("test-image", ".png");
        byte[] randomBytes = new byte[1024];
        new Random().nextBytes(randomBytes);
        Files.write(tempFile.toPath(), randomBytes);
        FilePart filePart = new FileSystemFilePart(tempFile, "files");
        Flux<FilePart> fileParts = Flux.just(filePart);
        MetadataDto metadataDto = new MetadataDto("img.png", FileType.IMAGE, "1");
        ServiceCallFailedException ex = new ServiceCallFailedException(
                "Error uploading files to file-service", "file-service", "/upload");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );

        StepVerifier.create(fileClient.uploadFiles(fileParts, metadataDto))
                .expectErrorMatches(throwable -> {
                    assertInstanceOf(ServiceCallFailedException.class, throwable);
                    ServiceCallFailedException serviceCallFailedException = (ServiceCallFailedException) throwable;
                    assertEquals(ex.getMessage(), serviceCallFailedException.getMessage());
                    assertEquals(ex.getServiceName(), serviceCallFailedException.getServiceName());
                    assertEquals(ex.getServicePath(), serviceCallFailedException.getServicePath());
                    return true;
                })
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/files/upload", recordedRequest.getPath());
        assertEquals("*/*", recordedRequest.getHeader("Accept"));
        assertEquals(3, mockWebServer.getRequestCount());

        filePart.delete().block();
    }

    @Test
    @SneakyThrows
    void deleteFiles_success() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NO_CONTENT.value())
        );
        var urls = List.of("https://im51.go.ro/files/123");

        StepVerifier.create(fileClient.deleteFiles(urls))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("DELETE", recordedRequest.getMethod());
        assertEquals("/api/files/delete", recordedRequest.getPath());
        assertEquals("*/*", recordedRequest.getHeader("Accept"));
        assertEquals(1, mockWebServer.getRequestCount());
        var body = objectMapper.readValue(recordedRequest.getBody().readUtf8(),
                new TypeReference<GridIdsDto>() {
                });

        assertEquals(new GridIdsDto(List.of("123")), body);

    }

    @Test
    @SneakyThrows
    void deleteFiles_error() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        var urls = List.of("https://im51.go.ro/files/123");

        StepVerifier.create(fileClient.deleteFiles(urls))
                .expectError(ServiceCallFailedException.class)
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("DELETE", recordedRequest.getMethod());
        assertEquals("/api/files/delete", recordedRequest.getPath());
        assertEquals("*/*", recordedRequest.getHeader("Accept"));
        assertEquals(3, mockWebServer.getRequestCount());


    }


}