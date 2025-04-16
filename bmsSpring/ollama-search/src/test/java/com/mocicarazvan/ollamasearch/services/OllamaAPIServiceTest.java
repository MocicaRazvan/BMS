package com.mocicarazvan.ollamasearch.services;

import com.mocicarazvan.ollamasearch.cache.EmbedCache;
import com.mocicarazvan.ollamasearch.clients.OllamaAPI;
import com.mocicarazvan.ollamasearch.config.OllamaPropertiesConfig;
import com.mocicarazvan.ollamasearch.dtos.OllamaOptions;
import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedRequestModel;
import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedResponseModel;
import com.mocicarazvan.ollamasearch.services.impl.OllamaAPIServiceImpl;
import com.mocicarazvan.ollamasearch.utils.OllamaQueryUtils;
import com.mocicarazvan.ollamasearch.utils.TextPreprocessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OllamaAPIServiceTest {


    private OllamaPropertiesConfig ollamaPropertiesConfig;

    @Mock
    private OllamaAPI ollamaAPI;

    @Mock
    private OllamaQueryUtils ollamaQueryUtils;

    private OllamaAPIServiceImpl service;

    @Mock
    private EmbedCache embedCache;


    @BeforeEach
    void setUp() {
        ollamaPropertiesConfig = new OllamaPropertiesConfig();
        ollamaPropertiesConfig.setUrl("http://localhost:11434");
        ollamaPropertiesConfig.setEmbeddingModel("bge-m3");
        service = new OllamaAPIServiceImpl(ollamaPropertiesConfig, ollamaAPI, ollamaQueryUtils);
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n", " \r\n "})
    void isNotNullOrEmpty_false(String input) {
        assertFalse(service.isNotNullOrEmpty(input));
    }

    @ParameterizedTest
    @ValueSource(strings =
            {"a", "abc", "123", "abc123", "a b c"})
    void isNotNullOrEmpty_true(String input) {
        assertTrue(service.isNotNullOrEmpty(input));
    }

    @Test
    void convertToFloatPrimitive_convertsCorrectly() {
        Float[] input = new Float[]{1.1f, 2.2f, 3.3f};
        float[] expected = new float[]{1.1f, 2.2f, 3.3f};

        float[] result = service.convertToFloatPrimitive(input);

        assertArrayEquals(expected, result);
    }

    @Test
    void convertToFloatPrimitive_emptyArray() {
        Float[] input = new Float[0];
        float[] result = service.convertToFloatPrimitive(input);
        assertEquals(0, result.length);
    }

    @Test
    void convertToFloatPrimitive_nullInput_throwsException() {
        assertThrows(NullPointerException.class, () -> service.convertToFloatPrimitive(null));
    }

    @Test
    void convertToFloat_convertsCorrectly() {
        List<Double> input = List.of(1.1, 2.2, 3.3);
        Float[] expected = new Float[]{1.1f, 2.2f, 3.3f};

        Float[] result = ReflectionTestUtils
                .invokeMethod(
                        service,
                        "convertToFloat",
                        input
                );

        assertArrayEquals(expected, result);
    }

    @Test
    void convertToFloat_emptyArray() {
        List<Double> input = List.of();
        Float[] result = ReflectionTestUtils
                .invokeMethod(
                        service,
                        "convertToFloat",
                        input
                );
        assertNotNull(result);
        assertEquals(0, result.length);
    }


    @Test
    void getFloats_convertsCorrectly() {
        List<Double> input = List.of(1.1, 2.2, 3.3);
        float[] expected = new float[]{1.1f, 2.2f, 3.3f};

        float[] result = ReflectionTestUtils.invokeMethod(
                service,
                "getFloats",
                input
        );

        assertArrayEquals(expected, result);
    }

    @Test
    void getFloats_emptyArray() {
        List<Double> input = List.of();
        float[] result =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "getFloats",
                        input
                );
        assertNotNull(result);
        assertEquals(0, result.length);
    }


    @Test
    void embed_list() {
        List<String> inputs = List.of("a", "b", "c");
        String[] preprocessedInputs = new String[]{"a", "b", "c"};
        try (MockedStatic<TextPreprocessor> textPreprocessor = mockStatic(TextPreprocessor.class)) {
            textPreprocessor.when(() -> TextPreprocessor.preprocess(inputs))
                    .thenReturn(preprocessedInputs);
            when(ollamaAPI.embed(any()))
                    .thenReturn(Mono.just(new OllamaEmbedResponseModel()));
            StepVerifier.create(service.generateEmbeddings(inputs))
                    .expectNextCount(1)
                    .verifyComplete();

            var reqCaptor = ArgumentCaptor.forClass(OllamaEmbedRequestModel.class);
            verify(ollamaAPI).embed(reqCaptor.capture());
            var expectedReq = OllamaEmbedRequestModel.builder()
                    .fromConfig(ollamaPropertiesConfig)
                    .input(preprocessedInputs)
                    .options(OllamaOptions.builder()
                            .fromConfig(ollamaPropertiesConfig)
                            .build())
                    .build();

            assertEquals(expectedReq, reqCaptor.getValue());
        }
    }

    @Test
    void embed_single() {
        var input = "a";
        String[] preprocessedInputs = new String[]{"a"};
        try (MockedStatic<TextPreprocessor> textPreprocessor = mockStatic(TextPreprocessor.class)) {

            textPreprocessor.when(() -> TextPreprocessor.preprocess(List.of(input)))
                    .thenReturn(preprocessedInputs);
            when(ollamaAPI.embed(any()))
                    .thenReturn(Mono.just(new OllamaEmbedResponseModel()));
            StepVerifier.create(service.generateEmbedding(input))
                    .expectNextCount(1)
                    .verifyComplete();

            var reqCaptor = ArgumentCaptor.forClass(OllamaEmbedRequestModel.class);
            verify(ollamaAPI).embed(reqCaptor.capture());
            var expectedReq = OllamaEmbedRequestModel.builder()
                    .fromConfig(ollamaPropertiesConfig)
                    .input(preprocessedInputs)
                    .options(OllamaOptions.builder()
                            .fromConfig(ollamaPropertiesConfig)
                            .build())
                    .build();

            assertEquals(expectedReq, reqCaptor.getValue());
        }
    }

    @Test
    void generateEmbeddingWithCache() {
        String text = "test";
        OllamaEmbedResponseModel mockResponse = new OllamaEmbedResponseModel();

        when(embedCache.getEmbedding(eq(text), any()))
                .thenReturn(Mono.just(mockResponse));

        StepVerifier.create(
                        service.generateEmbeddingWithCache(text, embedCache)
                                .map(result -> {
                                    assertTrue(Thread.currentThread().getName().contains("boundedElastic"),
                                            "Should be running on boundedElastic scheduler");
                                    return result;
                                })
                ).expectNext(mockResponse)
                .verifyComplete();

        verify(embedCache).getEmbedding(eq(text), any());
    }

    @Test
    void generateEmbeddingFloatMonoWithCache() {
        String text = "test";
        OllamaEmbedResponseModel mockResponse = new OllamaEmbedResponseModel();
        List<Double> embedding = List.of(1.0, 2.0, 3.0);
        Float[] expected = new Float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            expected[i] = embedding.get(i).floatValue();
        }
        mockResponse.setEmbeddings(List.of(embedding));

        when(embedCache.getEmbedding(eq(text), any()))
                .thenReturn(Mono.just(mockResponse));

        StepVerifier.create(service.generateEmbeddingFloatMonoWithCache(text, embedCache))
                .expectNextMatches(res -> Arrays.equals(res, expected))
                .verifyComplete();

    }

    @Test
    void generateEmbeddingFloatMono() {
        var input = "a";
        String[] preprocessedInputs = new String[]{"a"};
        try (MockedStatic<TextPreprocessor> textPreprocessor = mockStatic(TextPreprocessor.class)) {

            textPreprocessor.when(() -> TextPreprocessor.preprocess(List.of(input)))
                    .thenReturn(preprocessedInputs);
            OllamaEmbedResponseModel mockResponse = new OllamaEmbedResponseModel();
            List<Double> embedding = List.of(1.0, 2.0, 3.0);
            Float[] expected = new Float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                expected[i] = embedding.get(i).floatValue();
            }
            mockResponse.setEmbeddings(List.of(embedding));
            when(ollamaAPI.embed(any()))
                    .thenReturn(Mono.just(mockResponse));
            StepVerifier.create(service.generateEmbeddingFloatMono(input))
                    .expectNextMatches(res -> Arrays.equals(res, expected))
                    .verifyComplete();

        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n", " \r\n "})
    void getEmbedding_emptySource(String input) {
        StepVerifier.create(service.getEmbedding(input, embedCache))
                .expectNext("")
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "abc", "123", "abc123", "a b c"})
    void generateEmbeddingWithCache_notEmptySource(String input) {
        OllamaEmbedResponseModel mockResponse = new OllamaEmbedResponseModel();
        List<Double> embedding = List.of(1.0, 2.0, 3.0);
        mockResponse.setEmbeddings(List.of(embedding));
        when(embedCache.getEmbedding(eq(input), any()))
                .thenReturn(Mono.just(mockResponse));
        when(ollamaQueryUtils.getEmbeddingsAsString(mockResponse))
                .thenReturn(input);

        StepVerifier.create(service.getEmbedding(input, embedCache))
                .expectNext(input)
                .verifyComplete();

    }


}