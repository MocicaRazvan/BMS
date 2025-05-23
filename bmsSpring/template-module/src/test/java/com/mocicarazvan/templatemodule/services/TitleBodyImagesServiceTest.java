package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.WithUserDtoImpl;
import com.mocicarazvan.templatemodule.dtos.files.MetadataDto;
import com.mocicarazvan.templatemodule.dtos.response.FileUploadResponse;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.TitleBodyImages;
import com.mocicarazvan.templatemodule.models.TitleBodyImagesImpl;
import com.mocicarazvan.templatemodule.repositories.AssociativeEntityRepository;
import com.mocicarazvan.templatemodule.repositories.TitleBodyImagesRepository;
import com.mocicarazvan.templatemodule.services.impl.TitleBodyImagesServiceImpl;
import com.mocicarazvan.templatemodule.testUtils.AssertionTestUtils;
import com.mocicarazvan.templatemodule.testUtils.FilePartTestUtils;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TitleBodyImagesServiceTest {
    @Mock
    TitleBodyImagesRepository<TitleBodyImagesImpl> repository;

    @Mock
    DtoMapper<TitleBodyImagesImpl, WithUserDtoImpl, WithUserDtoImpl> mapper;

    @Mock
    UserClient userClient;

    @Spy
    PageableUtilsCustom pageableUtils;

    @Mock
    RabbitMqUpdateDeleteService<TitleBodyImagesImpl> rabbitMqUpdateDeleteService;

    @Spy
    EntitiesUtils entitiesUtils;

    @Mock
    FileClient fileClient;

    @Mock
    private AssociativeEntityRepository userLikesRepository;

    @Mock
    private AssociativeEntityRepository userDislikesRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    private final UserDto user1 = UserDto.builder().id(1L)
            .firstName("John")
            .lastName("Doe")
            .updatedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .email("john@doe.com")
            .role(Role.ROLE_ADMIN)
            .build();

    private final UserDto user2 = UserDto.builder().id(2L)
            .firstName("Jane")
            .lastName("Doe")
            .updatedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .email("jane@doe.com")
            .role(Role.ROLE_TRAINER)
            .build();

    private final TitleBodyImagesImpl model1 = TitleBodyImagesImpl.builder()
            .id(1L)
            .userId(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .title("Title 1")
            .body("Body 1")
            .images(List.of("http://example.com/image1.jpg"))
            .build();

    private final TitleBodyImagesImpl model2 = TitleBodyImagesImpl.builder()
            .id(2L)
            .userId(2L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .title("Title 2")
            .body("Body 2")
            .images(List.of("http://example.com/image2.jpg"))
            .build();

    private final WithUserDtoImpl bodyResp1 = WithUserDtoImpl.builder()
            .id(1L)
            .userId(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    private final WithUserDtoImpl bodyResp2 = WithUserDtoImpl.builder()
            .id(2L)
            .userId(2L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    private TitleBodyImagesServiceImpl.TitleBodyImagesServiceRedisCacheWrapper<TitleBodyImagesImpl, WithUserDtoImpl, WithUserDtoImpl,
            TitleBodyImagesRepository<TitleBodyImagesImpl>, DtoMapper<TitleBodyImagesImpl, WithUserDtoImpl, WithUserDtoImpl>
            > cacheWrapper;
    @Mock
    private TitleBodyImagesServiceImpl.TitleBodyImagesServiceRedisCacheWrapper<TitleBodyImagesImpl, WithUserDtoImpl, WithUserDtoImpl,
            TitleBodyImagesRepository<TitleBodyImagesImpl>, DtoMapper<TitleBodyImagesImpl, WithUserDtoImpl, WithUserDtoImpl>
            > mockedCacheWrapper;

    private TitleBodyImagesServiceImpl<TitleBodyImagesImpl, WithUserDtoImpl, WithUserDtoImpl,
            TitleBodyImagesRepository<TitleBodyImagesImpl>, DtoMapper<TitleBodyImagesImpl, WithUserDtoImpl, WithUserDtoImpl>,
            TitleBodyImagesServiceImpl.TitleBodyImagesServiceRedisCacheWrapper<TitleBodyImagesImpl, WithUserDtoImpl, WithUserDtoImpl,
                    TitleBodyImagesRepository<TitleBodyImagesImpl>, DtoMapper<TitleBodyImagesImpl, WithUserDtoImpl, WithUserDtoImpl>
                    >
            > service;
    private static final String MODEL_NAME = "titleBody";
    private static final List<String> ALLOWED_SORTING_FIELDS = List.of("createdAt", "updatedAt", "id", "title", "body");
    private FilePart filePart1;
    private FilePart filePart2;
    private final List<String> imageUrls = List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg");


    @BeforeEach
    @SneakyThrows
    void setUp() {
        cacheWrapper = new TitleBodyImagesServiceImpl.TitleBodyImagesServiceRedisCacheWrapper<>(
                repository, mapper, MODEL_NAME, userClient
        );
        service = new TitleBodyImagesServiceImpl<>(
                repository, mapper, pageableUtils, userClient, MODEL_NAME, ALLOWED_SORTING_FIELDS,
                entitiesUtils, fileClient, mockedCacheWrapper, rabbitMqUpdateDeleteService,
                transactionalOperator, userLikesRepository, userDislikesRepository
        ) {
            @Override
            public TitleBodyImagesImpl cloneModel(TitleBodyImagesImpl titleBodyImages) {
                return titleBodyImages.clone();
            }
        };

        filePart1 = FilePartTestUtils.createFilePart("test-image1");
        filePart2 = FilePartTestUtils.createFilePart("test-image2");
    }

    @AfterEach
    void tearDown() {
        filePart1.delete().block();
        filePart2.delete().block();
    }

    @Test
    @Order(1)
    void loads() {
        assertNotNull(service);
        assertNotNull(repository);
        assertNotNull(userClient);
        assertNotNull(pageableUtils);
        assertNotNull(entitiesUtils);
        assertNotNull(fileClient);
        assertNotNull(rabbitMqUpdateDeleteService);
    }

    /*
            CACHE WRAPPER TESTS
     */

    /*
            SERVICE TESTS
     */
    @ParameterizedTest
    @EnumSource(FileType.class)
    void uploadFiles(FileType type) {
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        var fr = new FileUploadResponse(imageUrls, type);
        when(fileClient.uploadFiles(eq(parts), any(MetadataDto.class))).thenReturn(Mono.just(fr));
        var expectedMetadata = new MetadataDto();
        expectedMetadata.setName(type.toString() + " " + MODEL_NAME);
        expectedMetadata.setFileType(type);
        expectedMetadata.setClientId(clientId);

        StepVerifier.create(
                        Objects.requireNonNull(
                                ReflectionTestUtils.invokeMethod(service, "uploadFiles", parts, type, clientId)
                        )
                )
                .expectSubscription()
                .expectNext(fr)
                .verifyComplete();
        var args = ArgumentCaptor.forClass(MetadataDto.class);
        verify(fileClient).uploadFiles(eq(parts), args.capture());

        assertEquals(expectedMetadata, args.getValue());
    }

    @Test
    void getModelToBeCreatedWithImages_notNullLikesDislikes() {
        var model = model1.clone();
        model.setUserLikes(List.of());
        model.setUserDislikes(List.of());
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        var fr = new FileUploadResponse(imageUrls, FileType.IMAGE);
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(fileClient.uploadFiles(eq(parts), any(MetadataDto.class))).thenReturn(Mono.just(fr));
        when(mapper.fromBodyToModel(bodyResp1)).thenReturn(model);

        StepVerifier.create(
                        Objects.requireNonNull(
                                ReflectionTestUtils.invokeMethod(service, "getModelToBeCreatedWithImages", parts, bodyResp1, "1", clientId)
                        )
                )
                .expectNextMatches(resp -> {
                    if (resp instanceof TitleBodyImages titleBodyImages) {
                        assertEquals(List.of(), titleBodyImages.getUserLikes());
                        assertEquals(List.of(), titleBodyImages.getUserDislikes());
                        assertEquals(imageUrls, titleBodyImages.getImages());
                        assertEquals(user1.getId(), titleBodyImages.getUserId());
                        return true;
                    }

                    return false;
                })
                .verifyComplete();

    }

    @Test
    void getModelToBeCreatedWithImages_nullLikesDislikes() {
        var model = model1.clone();
        model.setUserLikes(null);
        model.setUserDislikes(null);
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        var fr = new FileUploadResponse(imageUrls, FileType.IMAGE);
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(fileClient.uploadFiles(eq(parts), any(MetadataDto.class))).thenReturn(Mono.just(fr));
        when(mapper.fromBodyToModel(bodyResp1)).thenReturn(model);

        StepVerifier.create(
                        Objects.requireNonNull(
                                ReflectionTestUtils.invokeMethod(service, "getModelToBeCreatedWithImages", parts, bodyResp1, "1", clientId)
                        )
                )
                .expectNextMatches(resp -> {
                    if (resp instanceof TitleBodyImages titleBodyImages) {
                        assertEquals(List.of(), titleBodyImages.getUserLikes());
                        assertEquals(List.of(), titleBodyImages.getUserDislikes());
                        assertEquals(imageUrls, titleBodyImages.getImages());
                        assertEquals(user1.getId(), titleBodyImages.getUserId());
                        return true;
                    }

                    return false;
                })
                .verifyComplete();
    }

    @Test
    void updateModelWithImages_success() {
        var model = model1.clone();
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(repository.findById(1L)).thenReturn(Mono.just(model));
        when(mockedCacheWrapper.isNotAuthor(model, user1)).thenReturn(Mono.just(false));
        doNothing().when(rabbitMqUpdateDeleteService).sendUpdateMessage(eq(model));
        when(repository.save(model)).thenReturn(Mono.just(model));
        when(mapper.fromModelToResponse(model)).thenReturn(bodyResp1);
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        var fr = new FileUploadResponse(imageUrls, FileType.IMAGE);
        when(fileClient.deleteFiles(model.getImages())).thenReturn(Mono.empty());
        when(fileClient.uploadFiles(eq(parts), any(MetadataDto.class))).thenReturn(Mono.just(fr));
        when(mapper.updateModelFromBody(bodyResp1, model)).thenReturn(Mono.just(model));

        StepVerifier.create(service.updateModelWithImages(parts, 1L, bodyResp1, "1", clientId))
                .expectNext(bodyResp1)
                .verifyComplete();

        var captor = ArgumentCaptor.forClass(TitleBodyImagesImpl.class);
        verify(mapper).fromModelToResponse(captor.capture());
        assertEquals(imageUrls, captor.getValue().getImages());
    }

    @Test
    void updateModelWithImages_notFound() {
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(repository.findById(1L)).thenReturn(Mono.empty());
        StepVerifier.create(service.updateModelWithImages(parts, 1L, bodyResp1, "1", clientId))
                .expectErrorMatches(t -> AssertionTestUtils.assertNotFound(t, MODEL_NAME, model1))
                .verify();
    }

    @Test
    void updateModelWithImages_privateRoute() {
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        when(userClient.getUser("", "2")).thenReturn(Mono.just(user2));
        when(repository.findById(1L)).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.isNotAuthor(model1, user2)).thenReturn(Mono.just(true));
        StepVerifier.create(service.updateModelWithImages(parts, 1L, bodyResp1, "2", clientId))
                .expectError(PrivateRouteException.class)
                .verify();
    }

    @Test
    void createModel_notNullLikes() {
        var model = model1.clone();
        model.setUserLikes(List.of());
        model.setUserDislikes(List.of());
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        var fr = new FileUploadResponse(imageUrls, FileType.IMAGE);
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(fileClient.uploadFiles(eq(parts), any(MetadataDto.class))).thenReturn(Mono.just(fr));
        when(mapper.fromBodyToModel(bodyResp1)).thenReturn(model);
        when(repository.save(any(TitleBodyImagesImpl.class))).thenAnswer(i -> Mono.just(i.getArguments()[0]));
        when(mapper.fromModelToResponse(any(TitleBodyImagesImpl.class))).thenReturn(bodyResp1);

        StepVerifier.create(service.createModel(parts, bodyResp1, "1", clientId))
                .expectNextCount(1)
                .verifyComplete();

        var captor = ArgumentCaptor.forClass(TitleBodyImagesImpl.class);
        verify(mapper).fromModelToResponse(captor.capture());
        assertEquals(imageUrls, captor.getValue().getImages());
        assertEquals(List.of(), captor.getValue().getUserLikes());
        assertEquals(List.of(), captor.getValue().getUserDislikes());
    }

    @Test
    void createModel_nullLikes() {
        var model = model1.clone();
        model.setUserLikes(null);
        model.setUserDislikes(null);
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        var fr = new FileUploadResponse(imageUrls, FileType.IMAGE);
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(fileClient.uploadFiles(eq(parts), any(MetadataDto.class))).thenReturn(Mono.just(fr));
        when(mapper.fromBodyToModel(bodyResp1)).thenReturn(model);
        when(repository.save(any(TitleBodyImagesImpl.class))).thenAnswer(i -> Mono.just(i.getArguments()[0]));
        when(mapper.fromModelToResponse(any(TitleBodyImagesImpl.class))).thenReturn(bodyResp1);

        StepVerifier.create(service.createModel(parts, bodyResp1, "1", clientId))
                .expectNextCount(1)
                .verifyComplete();

        var captor = ArgumentCaptor.forClass(TitleBodyImagesImpl.class);
        verify(mapper).fromModelToResponse(captor.capture());
        assertEquals(imageUrls, captor.getValue().getImages());
        assertEquals(List.of(), captor.getValue().getUserLikes());
        assertEquals(List.of(), captor.getValue().getUserDislikes());
    }
}