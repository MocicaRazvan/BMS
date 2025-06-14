package com.mocicarazvan.fileservice.controllers;

import com.mocicarazvan.fileservice.config.ObjectMapperConfig;
import com.mocicarazvan.fileservice.dtos.FileUploadResponse;
import com.mocicarazvan.fileservice.enums.FileType;
import com.mocicarazvan.fileservice.service.MediaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.Collections;


@WebFluxTest(controllers = FileController.class)
@Import(ObjectMapperConfig.class)
class FileControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MediaService mediaService;

    @Test
    void testUpload_withValidMetadata_returnsOk() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("metadata", """
                {
                    "name": "myfile.jpg",
                    "fileType": "IMAGE",
                    "clientId": "abc123"
                }
                """).headers(headers -> headers.setContentType(MediaType.APPLICATION_JSON));

        Mockito.when(mediaService.uploadFiles(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(new FileUploadResponse(Collections.emptyList(), FileType.IMAGE)));

        webTestClient.post()
                .uri("/files/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.files").exists();
    }

    @Test
    void testUpload_withInvalidMetadata_returnsBadRequest() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("metadata", """
                {
                    "name": "",
                    "clientId": "1232"
                }
                """).headers(headers -> headers.setContentType(MediaType.APPLICATION_JSON));

        webTestClient.post()
                .uri("/files/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Validation failed")
                .jsonPath("$.details.name").isEqualTo("Name cannot be blank")
                .jsonPath("$.details.fileType").isEqualTo("File type cannot be null");
    }
}