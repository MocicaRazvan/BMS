package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.ApproveDtoImpl;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.TitleBodyDtoImpl;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.files.MetadataDto;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.ApproveImpl;
import com.mocicarazvan.templatemodule.repositories.ApprovedRepository;
import com.mocicarazvan.templatemodule.repositories.AssociativeEntityRepository;
import com.mocicarazvan.templatemodule.services.impl.ApprovedServiceImpl;
import com.mocicarazvan.templatemodule.testUtils.AssertionTestUtils;
import com.mocicarazvan.templatemodule.testUtils.FilePartTestUtils;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.SneakyThrows;
import org.jooq.lambda.function.Function3;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApprovedServiceTest {

    @Mock
    ApprovedRepository<ApproveImpl> repository;

    @Mock
    DtoMapper<ApproveImpl, TitleBodyDtoImpl, ApproveDtoImpl> mapper;

    @Mock
    UserClient userClient;

    @Spy
    PageableUtilsCustom pageableUtils;

    @Mock
    RabbitMqApprovedSender<ApproveDtoImpl> rabbitMqApprovedSender;

    @Mock
    RabbitMqUpdateDeleteService<ApproveImpl> rabbitMqUpdateDeleteService;

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
    private final UserDto user3 = UserDto.builder()
            .id(3L)
            .firstName("Jack")
            .lastName("Smith")
            .updatedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .email("jack@smith.com")
            .role(Role.ROLE_USER)
            .build();

    private final UserDto user4 = UserDto.builder()
            .id(4L)
            .firstName("Jill")
            .lastName("Johnson")
            .updatedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .email("jill@jhonson.com")
            .role(Role.ROLE_USER)
            .build();
    private final ApproveImpl model1 = ApproveImpl.builder()
            .id(1L)
            .userId(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .title("Title 1")
            .body("Body 1")
            .userLikes(List.of(3L))
            .userDislikes(List.of(4L))
            .images(List.of("http://example.com/image1.jpg"))
            .approved(true)
            .build();

    private final ApproveImpl model2 = ApproveImpl.builder()
            .id(2L)
            .userId(2L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .title("Title 2")
            .body("Body 2")
            .userLikes(List.of(4L))
            .userDislikes(List.of(3L))
            .images(List.of("http://example.com/image2.jpg"))
            .approved(false)
            .build();

    private final TitleBodyDtoImpl body1 = TitleBodyDtoImpl.builder()
            .title("Title 1")
            .body("Body 1")
            .build();

    private final TitleBodyDtoImpl body2 = TitleBodyDtoImpl.builder()
            .title("Title 2")
            .body("Body 2")
            .build();

    private final ApproveDtoImpl resp1 = ApproveDtoImpl.builder()
            .id(1L)
            .userId(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .approved(false)
            .images(List.of("http://example.com/image1.jpg"))
            .build();

    private final ApproveDtoImpl resp2 = ApproveDtoImpl.builder()
            .id(2L)
            .userId(2L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .approved(true)
            .images(List.of("http://example.com/image2.jpg"))
            .build();

    private FilePart filePart1;
    private FilePart filePart2;
    private final List<String> imageUrls = List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg");


    private final PageableBody pageableBody = PageableBody.builder()
            .page(0)
            .size(2)
            .sortingCriteria(
                    ALLOWED_SORTING_FIELDS.stream().map(i -> Map.entry(i, "asc"))
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (a, _) -> a,
                                    LinkedHashMap::new
                            ))
            )
            .build();

    private ApprovedServiceImpl.ApprovedServiceRedisCacheWrapper<ApproveImpl, TitleBodyDtoImpl, ApproveDtoImpl,
            ApprovedRepository<ApproveImpl>, DtoMapper<ApproveImpl, TitleBodyDtoImpl, ApproveDtoImpl>
            > cacheWrapper;
    @Mock
    private ApprovedServiceImpl.ApprovedServiceRedisCacheWrapper<ApproveImpl, TitleBodyDtoImpl, ApproveDtoImpl,
            ApprovedRepository<ApproveImpl>, DtoMapper<ApproveImpl, TitleBodyDtoImpl, ApproveDtoImpl>
            > mockedCacheWrapper;

    private ApprovedServiceImpl<ApproveImpl, TitleBodyDtoImpl, ApproveDtoImpl,
            ApprovedRepository<ApproveImpl>, DtoMapper<ApproveImpl, TitleBodyDtoImpl, ApproveDtoImpl>,
            ApprovedServiceImpl.ApprovedServiceRedisCacheWrapper<ApproveImpl, TitleBodyDtoImpl, ApproveDtoImpl,
                    ApprovedRepository<ApproveImpl>, DtoMapper<ApproveImpl, TitleBodyDtoImpl, ApproveDtoImpl>
                    >
            > service;

    private static final String MODEL_NAME = "approvedModel";
    private static final List<String> ALLOWED_SORTING_FIELDS = List.of("createdAt", "updatedAt", "id", "title", "body");

    @BeforeEach
    @SneakyThrows
    void setup() {
        cacheWrapper = new ApprovedServiceImpl.ApprovedServiceRedisCacheWrapper<>(
                repository, mapper, MODEL_NAME, pageableUtils, userClient
        );

        service = new ApprovedServiceImpl<>(
                repository, mapper, pageableUtils,
                userClient, MODEL_NAME, ALLOWED_SORTING_FIELDS,
                entitiesUtils, fileClient, rabbitMqApprovedSender,
                mockedCacheWrapper, rabbitMqUpdateDeleteService,
                transactionalOperator, userLikesRepository, userDislikesRepository
        ) {
            @Override
            public ApproveImpl cloneModel(ApproveImpl approve) {
                return approve.clone();
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
        assertNotNull(cacheWrapper);
        assertNotNull(userClient);
        assertNotNull(pageableUtils);
        assertNotNull(rabbitMqApprovedSender);
        assertNotNull(rabbitMqUpdateDeleteService);
    }

      /*
            CACHE WRAPPER TESTS
     */

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateDeleteInvalidate(boolean app) {
        StepVerifier.create(cacheWrapper.updateDeleteInvalidate(Pair.of(resp1, app)))
                .expectNextMatches(n -> n.getFirst().equals(resp1) && n.getSecond().equals(app))
                .verifyComplete();
    }

    @Test
    void createInvalidate() {
        StepVerifier.create(cacheWrapper.createInvalidate(resp1))
                .expectNextMatches(n -> n.getFirst().equals(resp1) && n.getSecond().equals(Boolean.FALSE))
                .verifyComplete();
    }

    @Test
    void getModelsAuthorBase_success() {
        when(userClient.existsTrainerOrAdmin("/exists", user2.getId())).thenReturn(Mono.empty());
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(userClient.hasPermissionToModifyEntity(user1, user2.getId())).thenReturn(Mono.just(true));
        AtomicBoolean cbCalled = new AtomicBoolean(false);

        Function<PageRequest, Flux<PageableResponse<ApproveDtoImpl>>> cb = pr -> {
            cbCalled.set(true);
            return Flux.just();
        };

        StepVerifier.create(cacheWrapper.getModelsAuthorBase(user2.getId(), pageableBody, user1.getId().toString(), cb, ALLOWED_SORTING_FIELDS))
                .verifyComplete();

        assertTrue(cbCalled.get());

    }

    @Test
    void getModelsAuthorBase_throwsPrivateRoute() {
        when(userClient.existsTrainerOrAdmin("/exists", user2.getId())).thenReturn(Mono.empty());
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(userClient.hasPermissionToModifyEntity(user1, user2.getId())).thenReturn(Mono.just(false));
        AtomicBoolean cbCalled = new AtomicBoolean(false);

        Function<PageRequest, Flux<PageableResponse<ApproveDtoImpl>>> cb = pr -> {
            cbCalled.set(true);
            return Flux.just();
        };

        StepVerifier.create(cacheWrapper.getModelsAuthorBase(user2.getId(), pageableBody, user1.getId().toString(), cb, ALLOWED_SORTING_FIELDS))
                .expectError(PrivateRouteException.class)
                .verify();

        assertFalse(cbCalled.get());

    }

    @Test
    void protectRoute_success_approvedNotAdmin() {
        when(userClient.getUser("", user2.getId().toString())).thenReturn(Mono.just(user2));
        StepVerifier.create(cacheWrapper.protectRoute(true, pageableBody, user2.getId().toString(), ALLOWED_SORTING_FIELDS))
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void protectRoute_throws_notApprovedNotAdmin() {
        when(userClient.getUser("", user2.getId().toString())).thenReturn(Mono.just(user2));
        StepVerifier.create(cacheWrapper.protectRoute(false, pageableBody, user2.getId().toString(), ALLOWED_SORTING_FIELDS))
                .expectError(PrivateRouteException.class)
                .verify();

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void protectRoute_success_admin(boolean app) {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        StepVerifier.create(cacheWrapper.protectRoute(app, pageableBody, user1.getId().toString(), ALLOWED_SORTING_FIELDS))
                .expectNextCount(1)
                .verifyComplete();

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getModelsTitleBase(boolean app) {
        var newTitle = "New Title";
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.findAllByTitleContainingIgnoreCaseAndApproved(newTitle, app, pageRequest)).thenReturn(Flux.just(model1, model2));
        when(repository.countAllByTitleContainingIgnoreCaseAndApproved(newTitle, app)).thenReturn(Mono.just(2L));
        when(mapper.fromModelToResponse(model1)).thenReturn(resp1);
        when(mapper.fromModelToResponse(model2)).thenReturn(resp2);

        StepVerifier.create(cacheWrapper.getModelsTitleBase(app, pageRequest, newTitle))
                .expectNextMatches(p -> p.getContent().equals(resp1))
                .expectNextMatches(p -> p.getContent().equals(resp2))
                .verifyComplete();
    }

    @Test
    void getModelByIdWithUserLikesAndDislikesBase_success_approvedModel() {
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(userClient.getUsersByIdIn("/byIds", model1.getUserLikes())).thenReturn(Flux.just(user3));
        when(userClient.getUsersByIdIn("/byIds", model1.getUserDislikes())).thenReturn(Flux.just(user4));
        when(mapper.fromModelToResponse(model1)).thenReturn(resp1);
        var expectedResponse = ResponseWithUserLikesAndDislikes.<ApproveDtoImpl>builder()
                .user(user1)
                .model(resp1)
                .userLikes(List.of(user3))
                .userDislikes(List.of(user4))
                .build();
        StepVerifier.create(cacheWrapper.getModelByIdWithUserLikesAndDislikesBase(model1.getId(), user1))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void getModelByIdWithUserLikesAndDislikesBase_success_notApprovedModel() {
        when(repository.findById(model2.getId())).thenReturn(Mono.just(model2));
        when(userClient.hasPermissionToModifyEntity(user1, model2.getUserId())).thenReturn(Mono.just(true));
        when(userClient.getUser("", user2.getId().toString())).thenReturn(Mono.just(user2));
        when(userClient.getUsersByIdIn("/byIds", model2.getUserLikes())).thenReturn(Flux.just(user3));
        when(userClient.getUsersByIdIn("/byIds", model2.getUserDislikes())).thenReturn(Flux.just(user4));
        when(mapper.fromModelToResponse(model2)).thenReturn(resp2);
        var expectedResponse = ResponseWithUserLikesAndDislikes.<ApproveDtoImpl>builder()
                .user(user2)
                .model(resp2)
                .userLikes(List.of(user3))
                .userDislikes(List.of(user4))
                .build();
        StepVerifier.create(cacheWrapper.getModelByIdWithUserLikesAndDislikesBase(model2.getId(), user1))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void getModelByIdWithUserLikesAndDislikesBase_throwsPrivateRoute_notApprovedModel() {
        when(repository.findById(model2.getId())).thenReturn(Mono.just(model2));
        when(userClient.hasPermissionToModifyEntity(user1, model2.getUserId())).thenReturn(Mono.just(false));
        StepVerifier.create(cacheWrapper.getModelByIdWithUserLikesAndDislikesBase(model2.getId(), user1))
                .expectError(PrivateRouteException.class)
                .verify();
    }

    @Test
    void getModelByIdWithUserLikesAndDislikesBase_throwsNotFound() {
        when(repository.findById(model2.getId())).thenReturn(Mono.empty());
        StepVerifier.create(cacheWrapper.getModelByIdWithUserLikesAndDislikesBase(model2.getId(), user1))
                .expectErrorMatches(t -> AssertionTestUtils.assertNotFound(t, MODEL_NAME, model2))
                .verify();
    }

    @Test
    void getModelByIdWithUserBase_success_approvedModel() {
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(mapper.fromModelToResponse(model1)).thenReturn(resp1);
        var expectedResponse = ResponseWithUserDto.<ApproveDtoImpl>builder()
                .user(user1)
                .model(resp1)
                .build();
        StepVerifier.create(cacheWrapper.getModelByIdWithUserBase(user1, model1.getId()))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void getModelByIdWithUserBase_success_notApprovedModel() {
        when(repository.findById(model2.getId())).thenReturn(Mono.just(model2));
        when(userClient.hasPermissionToModifyEntity(user1, model2.getUserId())).thenReturn(Mono.just(true));
        when(userClient.getUser("", user2.getId().toString())).thenReturn(Mono.just(user2));
        when(mapper.fromModelToResponse(model2)).thenReturn(resp2);
        var expectedResponse = ResponseWithUserDto.<ApproveDtoImpl>builder()
                .user(user2)
                .model(resp2)
                .build();
        StepVerifier.create(cacheWrapper.getModelByIdWithUserBase(user1, model2.getId()))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void getModelByIdWithUserBase_throwsPrivateRoute_notApprovedModel() {
        when(repository.findById(model2.getId())).thenReturn(Mono.just(model2));
        when(userClient.hasPermissionToModifyEntity(user1, model2.getUserId())).thenReturn(Mono.just(false));
        StepVerifier.create(cacheWrapper.getModelByIdWithUserBase(user1, model2.getId()))
                .expectError(PrivateRouteException.class)
                .verify();
    }

    @Test
    void getModelByIdWithUserBase_throwsNotFound() {
        when(repository.findById(model2.getId())).thenReturn(Mono.empty());
        StepVerifier.create(cacheWrapper.getModelByIdWithUserBase(user1, model2.getId()))
                .expectErrorMatches(t -> AssertionTestUtils.assertNotFound(t, MODEL_NAME, model2))
                .verify();
    }

      /*
            SERVICE TESTS
     */

    @Test
    void updateModelGetOriginalApproved_success() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.isNotAuthor(model1, user1)).thenReturn(Mono.just(false));
        doNothing().when(rabbitMqUpdateDeleteService).sendUpdateMessage(eq(model1));
        when(mapper.updateModelFromBody(body1, model1)).thenReturn(Mono.just(model1));
        when(repository.save(model1)).thenReturn(Mono.just(model1));
        when(mapper.fromModelToResponse(model1)).thenReturn(resp1);
        when(mockedCacheWrapper.updateDeleteInvalidate(any())).thenAnswer(i -> Mono.just(i.getArguments()[0]));

        StepVerifier.create(service.updateModelGetOriginalApproved(model1.getId(), body1, user1.getId().toString()))
                .expectNextMatches(resp ->
                        resp.getFirst().equals(resp1) && resp.getSecond().equals(model1.isApproved())
                )
                .verifyComplete();
    }

    @Test
    void updateModelGetOriginalApproved_throwsPrivateRoute() {
        when(userClient.getUser("", user2.getId().toString())).thenReturn(Mono.just(user2));
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.isNotAuthor(model1, user2)).thenReturn(Mono.just(true));

        StepVerifier.create(service.updateModelGetOriginalApproved(model1.getId(), body1, user2.getId().toString()))
                .expectError(PrivateRouteException.class)
                .verify();
    }

    @Test
    void updateModelGetOriginalApproved_throwsNotFound() {
        when(userClient.getUser("", user2.getId().toString())).thenReturn(Mono.just(user2));
        when(repository.findById(model1.getId())).thenReturn(Mono.empty());

        StepVerifier.create(service.updateModelGetOriginalApproved(model1.getId(), body1, user2.getId().toString()))
                .expectErrorMatches(t -> AssertionTestUtils.assertNotFound(t, MODEL_NAME, model1))
                .verify();
    }

    @Test
    void deleteModelGetOriginalApproved_success() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.privateRouteBase(true, user1, model1.getUserId())).thenReturn(Mono.empty());
        when(fileClient.deleteFiles(model1.getImages())).thenReturn(Mono.empty());
        when(repository.delete(model1)).thenReturn(Mono.empty());
        when(mapper.fromModelToResponse(model1)).thenReturn(resp1);
        doNothing().when(rabbitMqUpdateDeleteService).sendDeleteMessage(eq(model1));
        when(mockedCacheWrapper.updateDeleteInvalidate(any())).thenAnswer(i -> Mono.just(i.getArguments()[0]));

        StepVerifier.create(service.deleteModelGetOriginalApproved(model1.getId(), user1.getId().toString()))
                .expectNextMatches(resp ->
                        resp.getFirst().equals(resp1) && resp.getSecond().equals(model1.isApproved())
                )
                .verifyComplete();
    }

    @Test
    void deleteModelGetOriginalApproved_throwsNotFound() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model1.getId())).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteModelGetOriginalApproved(model1.getId(), user1.getId().toString()))
                .expectErrorMatches(t -> AssertionTestUtils.assertNotFound(t, MODEL_NAME, model1))
                .verify();
    }

    @Test
    void createModel_success() {
        var model = model1.clone();
        model.setUserId(null);
        model.setCreatedAt(null);
        model.setUpdatedAt(null);
        model.setUserLikes(null);
        model.setUserDislikes(List.of());
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(mapper.fromBodyToModel(body1)).thenReturn(model);
        when(repository.save(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(mapper.fromModelToResponse(model)).thenReturn(resp1);
        when(mockedCacheWrapper.createInvalidate(any())).thenAnswer(i -> Mono.just(Pair.of(i.getArguments()[0], Boolean.FALSE)));

        StepVerifier.create(service.createModel(body1, user1.getId().toString()))
                .expectNext(resp1)
                .verifyComplete();

        ArgumentCaptor<ApproveImpl> captor = ArgumentCaptor.forClass(ApproveImpl.class);
        verify(repository).save(captor.capture());
        ApproveImpl saved = captor.getValue();

        assertEquals(user1.getId(), saved.getUserId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(List.of(), saved.getUserLikes());
        assertEquals(List.of(), saved.getUserDislikes());
    }

    @Test
    void createModelImages_success() {
        var model = model1.clone();
        model.setUserId(null);
        model.setCreatedAt(null);
        model.setUpdatedAt(null);
        model.setUserLikes(null);
        model.setUserDislikes(List.of());
        var images = Flux.just(filePart1, filePart2);
        var clientId = "clientId";
        var fr = new FileUploadResponse(imageUrls, FileType.IMAGE);
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(fileClient.uploadFiles(eq(images), any(MetadataDto.class))).thenReturn(Mono.just(fr));
        when(mapper.fromBodyToModel(body1)).thenReturn(model);
        when(repository.save(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(mapper.fromModelToResponse(model)).thenReturn(resp1);
        when(mockedCacheWrapper.createInvalidate(any())).thenAnswer(i -> Mono.just(Pair.of(i.getArguments()[0], Boolean.FALSE)));

        StepVerifier.create(service.createModel(images, body1, user1.getId().toString(), clientId))
                .expectNext(resp1)
                .verifyComplete();

        ArgumentCaptor<ApproveImpl> modelCaptor = ArgumentCaptor.forClass(ApproveImpl.class);
        verify(repository).save(modelCaptor.capture());
        ApproveImpl saved = modelCaptor.getValue();

        ArgumentCaptor<MetadataDto> metadataCaptor = ArgumentCaptor.forClass(MetadataDto.class);
        verify(fileClient).uploadFiles(eq(images), metadataCaptor.capture());
        MetadataDto meta = metadataCaptor.getValue();

        assertEquals(user1.getId(), saved.getUserId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(List.of(), saved.getUserLikes());
        assertEquals(List.of(), saved.getUserDislikes());
        assertEquals(imageUrls, saved.getImages());


        assertEquals(FileType.IMAGE + " " + MODEL_NAME, meta.getName());
        assertEquals(FileType.IMAGE, meta.getFileType());
        assertEquals(clientId, meta.getClientId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateModelWithImagesGetOriginalApproved_success(boolean app) {
        var model = model1.clone();
        model.setApproved(app);
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        var fr = new FileUploadResponse(imageUrls, FileType.IMAGE);
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(repository.findById(1L)).thenReturn(Mono.just(model));
        when(mockedCacheWrapper.isNotAuthor(model, user1)).thenReturn(Mono.just(false));
        doNothing().when(rabbitMqUpdateDeleteService).sendUpdateMessage(eq(model));
        when(mapper.fromModelToResponse(model)).thenReturn(resp1);
        when(fileClient.deleteFiles(model.getImages())).thenReturn(Mono.empty());
        when(fileClient.uploadFiles(eq(parts), any(MetadataDto.class))).thenReturn(Mono.just(fr));
        when(mapper.updateModelFromBody(body1, model)).thenReturn(Mono.just(model));

        AtomicBoolean cbCalled = new AtomicBoolean(false);
        Function3<TitleBodyDtoImpl, String, ApproveImpl, Mono<ApproveImpl>> callback = (_, _, m) -> {
            cbCalled.set(true);
            return Mono.just(m);
        };

        when(mockedCacheWrapper.updateDeleteInvalidate(any())).thenAnswer(i -> Mono.just(i.getArguments()[0]));

        StepVerifier.create(service.updateModelWithImagesGetOriginalApproved(parts, 1L, body1, "1", clientId, callback))
                .expectNextMatches(p -> p.getFirst().equals(resp1)
                        && p.getSecond().equals(app)
                )
                .verifyComplete();

        var captor = ArgumentCaptor.forClass(ApproveImpl.class);
        verify(mapper).fromModelToResponse(captor.capture());
        assertEquals(imageUrls, captor.getValue().getImages());

        assertTrue(cbCalled.get());
    }

    @Test
    void updateModelWithImagesGetOriginalApproved_throwsNotFound() {
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(repository.findById(1L)).thenReturn(Mono.empty());
        AtomicBoolean cbCalled = new AtomicBoolean(false);
        Function3<TitleBodyDtoImpl, String, ApproveImpl, Mono<ApproveImpl>> callback = (_, _, m) -> {
            cbCalled.set(true);
            return Mono.just(m);
        };


        StepVerifier.create(service.updateModelWithImagesGetOriginalApproved(parts, 1L, body1, "1", clientId, callback))
                .expectErrorMatches(t -> AssertionTestUtils.assertNotFound(t, MODEL_NAME, model1))
                .verify();

        assertFalse(cbCalled.get());
    }

    @Test
    void updateModelWithImages_privateRoute() {
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        when(userClient.getUser("", "2")).thenReturn(Mono.just(user2));
        when(repository.findById(1L)).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.isNotAuthor(model1, user2)).thenReturn(Mono.just(true));

        AtomicBoolean cbCalled = new AtomicBoolean(false);
        Function3<TitleBodyDtoImpl, String, ApproveImpl, Mono<ApproveImpl>> callback = (_, _, m) -> {
            cbCalled.set(true);
            return Mono.just(m);
        };


        StepVerifier.create(service.updateModelWithImagesGetOriginalApproved(parts, 1L, body1, "2", clientId, callback))
                .expectError(PrivateRouteException.class)
                .verify();

        assertFalse(cbCalled.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateModelWithImagesGetOriginalApproved_success_noCb(boolean app) {
        var model = model1.clone();
        model.setApproved(app);
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        var fr = new FileUploadResponse(imageUrls, FileType.IMAGE);
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(repository.findById(1L)).thenReturn(Mono.just(model));
        when(mockedCacheWrapper.isNotAuthor(model, user1)).thenReturn(Mono.just(false));
        doNothing().when(rabbitMqUpdateDeleteService).sendUpdateMessage(eq(model));
        when(repository.save(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(mapper.fromModelToResponse(model)).thenReturn(resp1);
        when(fileClient.deleteFiles(model.getImages())).thenReturn(Mono.empty());
        when(fileClient.uploadFiles(eq(parts), any(MetadataDto.class))).thenReturn(Mono.just(fr));
        when(mapper.updateModelFromBody(body1, model)).thenReturn(Mono.just(model));

        when(mockedCacheWrapper.updateDeleteInvalidate(any())).thenAnswer(i -> Mono.just(i.getArguments()[0]));

        StepVerifier.create(service.updateModelWithImagesGetOriginalApproved(parts, 1L, body1, "1", clientId))
                .expectNextMatches(p -> p.getFirst().equals(resp1)
                        && p.getSecond().equals(app)
                )
                .verifyComplete();

        var captor = ArgumentCaptor.forClass(ApproveImpl.class);
        verify(mapper).fromModelToResponse(captor.capture());
        assertEquals(imageUrls, captor.getValue().getImages());
    }

    @Test
    void updateModelWithImagesGetOriginalApproved_throwsNotFound_noCb() {
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(repository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(service.updateModelWithImagesGetOriginalApproved(parts, 1L, body1, "1", clientId))
                .expectErrorMatches(t -> AssertionTestUtils.assertNotFound(t, MODEL_NAME, model1))
                .verify();

    }

    @Test
    void updateModelWithImages_privateRoute_noCb() {
        var clientId = "clientId";
        var parts = Flux.just(filePart1, filePart2);
        when(userClient.getUser("", "2")).thenReturn(Mono.just(user2));
        when(repository.findById(1L)).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.isNotAuthor(model1, user2)).thenReturn(Mono.just(true));

        StepVerifier.create(service.updateModelWithImagesGetOriginalApproved(parts, 1L, body1, "2", clientId))
                .expectError(PrivateRouteException.class)
                .verify();

    }

    @Test
    void getModelByIdWithOwner_success() {
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(repository.findById(model2.getId())).thenReturn(Mono.just(model2));
        when(mockedCacheWrapper.privateRouteBase(!model2.isApproved(), user1, model2.getUserId())).thenReturn(Mono.empty());
        when(mapper.fromModelToResponse(model2)).thenReturn(resp2);
        when(userClient.getUser("", "2")).thenReturn(Mono.just(user2));

        StepVerifier.create(service.getModelByIdWithOwner(model2.getId(), user1.getId().toString()))
                .expectNextMatches(t -> t.getT1().equals(resp2)
                        && t.getT2().equals(user2))
                .verifyComplete();
    }

    @Test
    void getModelByIdWithOwner_throwsNotFound() {
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(repository.findById(model2.getId())).thenReturn(Mono.empty());
        StepVerifier.create(service.getModelByIdWithOwner(model2.getId(), user1.getId().toString()))
                .expectErrorMatches(t -> AssertionTestUtils.assertNotFound(t, MODEL_NAME, model2))
                .verify();
    }

    @Test
    void getModelsAuthor() {
        Function<PageRequest, Flux<PageableResponse<ApproveDtoImpl>>> getResponse = _ -> Flux.just();
        when(mockedCacheWrapper.getModelsAuthorBase(user2.getId(), pageableBody, user1.getId().toString(), getResponse, ALLOWED_SORTING_FIELDS))
                .thenReturn(Flux.just());
        StepVerifier.create(
                        Objects.requireNonNull(
                                ReflectionTestUtils.invokeMethod(service, "getModelsAuthor", user2.getId(), pageableBody, user1.getId().toString(), getResponse)
                        )
                )
                .verifyComplete();
    }

    static Stream<String> titleProvider() {
        return Stream.of(null, "", "title");
    }

    @ParameterizedTest
    @MethodSource("titleProvider")
    void getModelsTrainer_nullApproved(String title) {
        when(repository.findAllByUserIdAndTitleContainingIgnoreCase(eq(user1.getId()), anyString(), any(PageRequest.class)))
                .thenReturn(Flux.just(model1));
        when(repository.countAllByUserIdAndTitleContainingIgnoreCase(eq(user1.getId()), anyString()))
                .thenReturn(Mono.just(1L));
        var pageableResponse = new PageableResponse<ApproveDtoImpl>();
        pageableResponse.setContent(resp1);
        when(mockedCacheWrapper.getModelsAuthorBase(eq(user1.getId()), eq(pageableBody),
                eq(user1.getId().toString()), any(Function.class), eq(ALLOWED_SORTING_FIELDS)))
                .thenAnswer(i -> {
                    PageRequest pr = PageRequest.of(0, 2);
                    Function<PageRequest, Flux<PageableResponse<ApproveDtoImpl>>> cb = i.getArgument(3);
                    return cb.apply(pr);
                });
        when(mapper.fromModelToResponse(model1)).thenReturn(resp1);

        StepVerifier.create(service.getModelsTrainer(title, user1.getId(), pageableBody, user1.getId().toString(), null))
                .expectNextMatches(p -> p.getContent().equals(resp1))
                .verifyComplete();

        var findTitleCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository).findAllByUserIdAndTitleContainingIgnoreCase(eq(user1.getId()), findTitleCaptor.capture(), any(PageRequest.class));
        var countTitleCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository).countAllByUserIdAndTitleContainingIgnoreCase(eq(user1.getId()), countTitleCaptor.capture());

        assertNotNull(findTitleCaptor.getValue());
        assertNotNull(countTitleCaptor.getValue());
        assertEquals(findTitleCaptor.getValue(), countTitleCaptor.getValue());
    }

    static Stream<Arguments> titleApproveProvider() {
        return Stream.of(null, "", "title")
                .flatMap(s -> Stream.of(
                        Arguments.of(s, true),
                        Arguments.of(s, false)
                ));
    }

    @ParameterizedTest
    @MethodSource("titleApproveProvider")
    void getModelsTrainer_NotNullApproved(String title, boolean approved) {
        when(repository.findAllByUserIdAndTitleContainingIgnoreCaseAndApproved(eq(user1.getId()), anyString(), eq(approved), any(PageRequest.class)))
                .thenReturn(Flux.just(model1));
        when(repository.countAllByUserIdAndTitleContainingIgnoreCaseAndApproved(eq(user1.getId()), anyString(), eq(approved)))
                .thenReturn(Mono.just(1L));
        var pageableResponse = new PageableResponse<ApproveDtoImpl>();
        pageableResponse.setContent(resp1);
        when(mockedCacheWrapper.getModelsAuthorBase(eq(user1.getId()), eq(pageableBody),
                eq(user1.getId().toString()), any(Function.class), eq(ALLOWED_SORTING_FIELDS)))
                .thenAnswer(i -> {
                    PageRequest pr = PageRequest.of(0, 2);
                    Function<PageRequest, Flux<PageableResponse<ApproveDtoImpl>>> cb = i.getArgument(3);
                    return cb.apply(pr);
                });
        when(mapper.fromModelToResponse(model1)).thenReturn(resp1);

        StepVerifier.create(service.getModelsTrainer(title, user1.getId(), pageableBody, user1.getId().toString(), approved))
                .expectNextMatches(p -> p.getContent().equals(resp1))
                .verifyComplete();

        var findTitleCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository).findAllByUserIdAndTitleContainingIgnoreCaseAndApproved(eq(user1.getId()), findTitleCaptor.capture(), eq(approved), any(PageRequest.class));
        var countTitleCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository).countAllByUserIdAndTitleContainingIgnoreCaseAndApproved(eq(user1.getId()), countTitleCaptor.capture(), eq(approved));

        assertNotNull(findTitleCaptor.getValue());
        assertNotNull(countTitleCaptor.getValue());
        assertEquals(findTitleCaptor.getValue(), countTitleCaptor.getValue());
    }

    @Test
    void getModelByIdWithUserLikesAndDislikes() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        var resp = ResponseWithUserLikesAndDislikes.<ApproveDtoImpl>builder()
                .user(user1)
                .model(resp1)
                .userLikes(List.of(user3))
                .userDislikes(List.of(user4))
                .build();
        when(mockedCacheWrapper.getModelByIdWithUserLikesAndDislikesBase(model1.getId(), user1)).thenReturn(Mono.just(resp));
        StepVerifier.create(service.getModelByIdWithUserLikesAndDislikes(model1.getId(), user1.getId().toString()))
                .expectNext(resp)
                .verifyComplete();
    }

    @Test
    void getModelByIdWithUser() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        var resp = ResponseWithUserDto.<ApproveDtoImpl>builder()
                .user(user1)
                .model(resp1)
                .build();
        when(mockedCacheWrapper.getModelByIdWithUserBase(user1, model1.getId())).thenReturn(Mono.just(resp));
        StepVerifier.create(service.getModelByIdWithUser(model1.getId(), user1.getId().toString()))
                .expectNext(resp)
                .verifyComplete();
    }

    @Test
    void getModelById() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(mockedCacheWrapper.getModel(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.privateRouteBase(!model1.isApproved(), user1, model1.getUserId())).thenReturn(Mono.empty());
        when(mapper.fromModelToResponse(model1)).thenReturn(resp1);

        StepVerifier.create(service.getModelById(model1.getId(), user1.getId().toString()))
                .expectNext(resp1)
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("titleProvider")
    void getAllModels(String title) {
        when(repository.findAllByTitleContainingIgnoreCase(anyString(), any(PageRequest.class)))
                .thenReturn(Flux.just(model1));
        when(mapper.fromModelToResponse(model1)).thenReturn(resp1);
        when(repository.countAllByTitleContainingIgnoreCase(anyString()))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(service.getAllModels(title, pageableBody, user1.getId().toString()))
                .expectNextMatches(p -> p.getContent().equals(resp1))
                .verifyComplete();

        var findTitleCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository).findAllByTitleContainingIgnoreCase(findTitleCaptor.capture(), any(PageRequest.class));

        var countTitleCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository).countAllByTitleContainingIgnoreCase(countTitleCaptor.capture());

        assertNotNull(findTitleCaptor.getValue());
        assertNotNull(countTitleCaptor.getValue());
        assertEquals(findTitleCaptor.getValue(), countTitleCaptor.getValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void protectRoute(boolean approved) {
        when(mockedCacheWrapper.protectRoute(approved, pageableBody, user1.getId().toString(), ALLOWED_SORTING_FIELDS))
                .thenReturn(Mono.empty());
        StepVerifier.create(
                        Objects.requireNonNull(
                                ReflectionTestUtils.invokeMethod(service, "protectRoute", approved, pageableBody, user1.getId().toString()
                                ))
                )
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("titleApproveProvider")
    void getModelsTitle(String title, boolean approved) {
        var pr = PageRequest.of(0, 2);
        when(mockedCacheWrapper.protectRoute(approved, pageableBody, user1.getId().toString(), ALLOWED_SORTING_FIELDS))
                .thenReturn(Mono.just(pr));

        when(mockedCacheWrapper.getModelsTitleBase(eq(approved), any(PageRequest.class), anyString()))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.getModelsTitle(title, approved, pageableBody, user1.getId().toString()))
                .verifyComplete();

        var titleCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedCacheWrapper).getModelsTitleBase(eq(approved), any(PageRequest.class), titleCaptor.capture());

        assertNotNull(titleCaptor.getValue());
    }

    @ParameterizedTest
    @MethodSource("titleApproveProvider")
    void getModelsWithUser(String title, boolean approved) {
        var pr = PageRequest.of(0, 2);
        var response = PageableResponse.<ApproveDtoImpl>builder()
                .content(resp1)
                .links(List.of())
                .pageInfo(PageInfo.builder()
                        .totalPages(1)
                        .totalElements(1)
                        .pageSize(2)
                        .currentPage(1)
                        .build())
                .build();
        when(mockedCacheWrapper.protectRoute(approved, pageableBody, user1.getId().toString(), ALLOWED_SORTING_FIELDS))
                .thenReturn(Mono.just(pr));

        when(mockedCacheWrapper.getModelsTitleBase(eq(approved), any(PageRequest.class), anyString()))
                .thenReturn(Flux.just(response));
        when(userClient.getUser("", model1.getUserId().toString()))
                .thenReturn(Mono.just(user1));

        StepVerifier.create(service.getModelsWithUser(title, pageableBody, user1.getId().toString(), approved))
                .expectNextMatches(p ->
                        p.getContent().getModel().equals(resp1)
                                && p.getPageInfo().equals(response.getPageInfo())
                                && p.getLinks().equals(response.getLinks())
                                && p.getContent().getUser().equals(user1)
                )
                .verifyComplete();

        var titleCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedCacheWrapper).getModelsTitleBase(eq(approved), any(PageRequest.class), titleCaptor.capture());
        assertNotNull(titleCaptor.getValue());

    }

    @ParameterizedTest
    @MethodSource("titleProvider")
    void getModelsApproved_titleAny(String title) {
        var pr = PageRequest.of(0, 2);
        var response = PageableResponse.<ApproveDtoImpl>builder()
                .content(resp1)
                .links(List.of())
                .pageInfo(PageInfo.builder()
                        .totalPages(1)
                        .totalElements(1)
                        .pageSize(2)
                        .currentPage(1)
                        .build())
                .build();
        when(mockedCacheWrapper.protectRoute(true, pageableBody, user1.getId().toString(), ALLOWED_SORTING_FIELDS))
                .thenReturn(Mono.just(pr));

        when(mockedCacheWrapper.getModelsTitleBase(eq(true), any(PageRequest.class), anyString()))
                .thenReturn(Flux.just(response));
        when(userClient.getUser("", model1.getUserId().toString()))
                .thenReturn(Mono.just(user1));

        StepVerifier.create(service.getModelsWithUser(title, pageableBody, user1.getId().toString(), true))
                .expectNextMatches(p ->
                        p.getContent().getModel().equals(resp1)
                                && p.getPageInfo().equals(response.getPageInfo())
                                && p.getLinks().equals(response.getLinks())
                                && p.getContent().getUser().equals(user1)
                )
                .verifyComplete();

        var titleCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedCacheWrapper).getModelsTitleBase(eq(true), any(PageRequest.class), titleCaptor.capture());
        assertNotNull(titleCaptor.getValue());

    }

    @Test
    void getModelsApproved_titleNull() {
        var pr = PageRequest.of(0, 2);
        var response = PageableResponse.<ApproveDtoImpl>builder()
                .content(resp1)
                .links(List.of())
                .pageInfo(PageInfo.builder()
                        .totalPages(1)
                        .totalElements(1)
                        .pageSize(2)
                        .currentPage(1)
                        .build())
                .build();
        when(mockedCacheWrapper.protectRoute(true, pageableBody, user1.getId().toString(), ALLOWED_SORTING_FIELDS))
                .thenReturn(Mono.just(pr));

        when(mockedCacheWrapper.getModelsTitleBase(eq(true), any(PageRequest.class), anyString()))
                .thenReturn(Flux.just(response));
        when(userClient.getUser("", model1.getUserId().toString()))
                .thenReturn(Mono.just(user1));

        StepVerifier.create(service.getModelsWithUser(null, pageableBody, user1.getId().toString(), true))
                .expectNextMatches(p ->
                        p.getContent().getModel().equals(resp1)
                                && p.getPageInfo().equals(response.getPageInfo())
                                && p.getLinks().equals(response.getLinks())
                                && p.getContent().getUser().equals(user1)
                )
                .verifyComplete();

        var titleCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedCacheWrapper).getModelsTitleBase(eq(true), any(PageRequest.class), titleCaptor.capture());
        assertNotNull(titleCaptor.getValue());

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void approveModel_success(boolean app) {
        var model = model1.clone();
        model.setApproved(false);
        var resp = ApproveDtoImpl.builder()
                .id(resp1.getId())
                .title(resp1.getTitle())
                .images(resp1.getImages())
                .createdAt(resp1.getCreatedAt())
                .updatedAt(resp1.getUpdatedAt())
                .userId(resp1.getUserId())
                .userLikes(resp1.getUserLikes())
                .userDislikes(resp1.getUserDislikes())
                .body(resp1.getBody())
                .title(resp1.getTitle())
                .build();

        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model.getId())).thenReturn(Mono.just(model));
        when(repository.save(any())).thenAnswer(i -> Mono.just(i.getArguments()[0]));
        when(mockedCacheWrapper.getModelGuardWithUserBase(user1, model, !app)).thenAnswer(i -> {
            var m = (ApproveImpl) i.getArgument(1);
            resp.setApproved(m.isApproved());

            return Mono.just(new ResponseWithUserDto<>(resp, user1));

        });
        when(mockedCacheWrapper.updateDeleteInvalidate(any())).thenAnswer(i -> Mono.just(i.getArguments()[0]));
        doNothing().when(rabbitMqApprovedSender).sendMessage(eq(app), any(), eq(user1));

        StepVerifier.create(service.approveModel(model.getId(), user1.getId().toString(), app))
                .assertNext(res -> {
                    assertEquals(user1, res.getUser());
                    assertEquals(app, res.getModel().isApproved());
                    assertEquals(model.getId(), res.getModel().getId());
                })
                .verifyComplete();

        var appCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(rabbitMqApprovedSender).sendMessage(appCaptor.capture(), any(), eq(user1));
        assertEquals(app, appCaptor.getValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void approveModel_throwsNotFound(boolean app) {
        var model = model1.clone();
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(service.approveModel(model.getId(), user1.getId().toString(), app))
                .expectErrorMatches(t -> AssertionTestUtils.assertNotFound(t, MODEL_NAME, model))
                .verify();

    }

    @Test
    void approveModel_throwsIllegalActionException_alreadyApproved() {
        var model = model1.clone();
        model.setApproved(true);
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model.getId())).thenReturn(Mono.just(model));

        StepVerifier.create(service.approveModel(model.getId(), user1.getId().toString(), true))
                .expectErrorMatches(t -> t instanceof IllegalActionException
                        && t.getMessage().equals(MODEL_NAME + " with id " + model.getId() + " is already approved!"))
                .verify();

    }


}