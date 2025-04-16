package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.clients.CountInParentClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.WithUserDtoImpl;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.ManyToOneUserImpl;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.testUtils.AssertionTestUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.test.util.ReflectionTestUtils;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ManyToOneUserServiceTest {

    @Mock
    ManyToOneUserRepository<ManyToOneUserImpl> repository;

    @Mock
    DtoMapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl> mapper;

    @Mock
    UserClient userClient;

    @Spy
    PageableUtilsCustom pageableUtils;

    @Mock
    RabbitMqUpdateDeleteService<ManyToOneUserImpl> rabbitMqUpdateDeleteService;

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

    private final ManyToOneUserImpl model1 = ManyToOneUserImpl.builder()
            .id(1L)
            .userId(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    private final ManyToOneUserImpl model2 = ManyToOneUserImpl.builder()
            .id(2L)
            .userId(2L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
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
    private ManyToOneUserServiceImpl.ManyToOneUserServiceRedisCacheWrapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl,
            ManyToOneUserRepository<ManyToOneUserImpl>, DtoMapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl>> cacheWrapper;
    @Mock
    private ManyToOneUserServiceImpl.ManyToOneUserServiceRedisCacheWrapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl,
            ManyToOneUserRepository<ManyToOneUserImpl>, DtoMapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl>> mockedCacheWrapper;

    private ManyToOneUserServiceImpl<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl,
            ManyToOneUserRepository<ManyToOneUserImpl>, DtoMapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl>, ManyToOneUserServiceImpl.ManyToOneUserServiceRedisCacheWrapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl,
            ManyToOneUserRepository<ManyToOneUserImpl>, DtoMapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl>>> service;

    @Mock
    private CountInParentClient countInParentClient;

    private static final String MODEL_NAME = "manyToOneUser";
    private static final List<String> ALLOWED_SORTING_FIELDS = List.of("createdAt", "updatedAt", "id");

    @BeforeEach
    void setUp() {
        cacheWrapper = new ManyToOneUserServiceImpl.ManyToOneUserServiceRedisCacheWrapper<>(
                repository,
                mapper,
                MODEL_NAME,
                userClient
        );
        service = new ManyToOneUserServiceImpl<>(
                repository,
                mapper,
                pageableUtils,
                userClient,
                MODEL_NAME,
                ALLOWED_SORTING_FIELDS,
                mockedCacheWrapper,
                rabbitMqUpdateDeleteService
        ) {
            @Override
            public ManyToOneUserImpl cloneModel(ManyToOneUserImpl manyToOneUser) {
                return manyToOneUser.clone();
            }
        };
    }

    @Test
    @Order(1)
    void loads() {
        assertNotNull(service);
        assertNotNull(cacheWrapper);
        assertNotNull(repository);
        assertNotNull(mapper);
        assertNotNull(userClient);
        assertNotNull(pageableUtils);
        assertNotNull(rabbitMqUpdateDeleteService);
    }

    /*
            CACHE WRAPPER TESTS
     */

    @Test
    void isNotAuthor_notAuthor() {

        StepVerifier.create(cacheWrapper.isNotAuthor(model2, user1))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isNotAuthor_author() {

        StepVerifier.create(cacheWrapper.isNotAuthor(model1, user1))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void privateRouteBase_hasPermissionToModifyOwner_guard() {
        var ownerId = 1L;
        when(userClient.hasPermissionToModifyEntity(user1, ownerId)).thenReturn(Mono.just(true));
        StepVerifier.create(cacheWrapper.privateRouteBase(true, user1, ownerId))
                .verifyComplete();
    }

    @Test
    void privateRouteBase_hasPermissionToModifyOwner_noGuard() {
        var ownerId = 1L;
        StepVerifier.create(cacheWrapper.privateRouteBase(false, user1, ownerId))
                .verifyComplete();
    }


    @Test
    void privateRouteBase_hasPermissionToModifyFalse_guardFalse() {
        var ownerId = 11L;
        StepVerifier.create(cacheWrapper.privateRouteBase(false, user1, ownerId))
                .verifyComplete();
    }

    @Test
    void privateRouteBase_hasPermissionToModifyFalse_guardTrue() {
        var ownerId = 11L;
        when(userClient.hasPermissionToModifyEntity(user1, ownerId)).thenReturn(Mono.just(false));
        StepVerifier.create(cacheWrapper.privateRouteBase(true, user1, ownerId))
                .expectError(PrivateRouteException.class)
                .verify();
    }

    @Test
    void getModelGuardWithUserBase_notThrow() {
        var resp = WithUserDtoImpl.builder().id(1L).build();
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(mapper.fromModelToResponse(model1)).thenReturn(resp);
        var expected = new ResponseWithUserDto<>(resp, user1);
        StepVerifier.create(cacheWrapper.getModelGuardWithUserBase(user1, model1, false))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void getModelGuardWithUserBase_throws() {
        when(userClient.hasPermissionToModifyEntity(user1, model2.getUserId())).thenReturn(Mono.just(false));
        StepVerifier.create(cacheWrapper.getModelGuardWithUserBase(user1, model2, true))
                .expectError(PrivateRouteException.class)
                .verify();
    }

    @Test
    void finAllById() {
        var models = List.of(model1, model2);
        when(repository.findAllById(List.of(1L, 2L)))
                .thenReturn(Flux.fromIterable(models));
        StepVerifier.create(cacheWrapper.findAllById(List.of(1L, 2L)))
                .expectNextSequence(models)
                .verifyComplete();
    }

    @Test
    void getModelFound() {
        when(repository.findById(1L))
                .thenReturn(Mono.just(model1));
        StepVerifier.create(cacheWrapper.getModel(1L))
                .expectNext(model1)
                .verifyComplete();
    }

    @Test
    void getModelNotFound() {
        when(repository.findById(1L))
                .thenReturn(Mono.empty());
        StepVerifier.create(cacheWrapper.getModel(1L))
                .expectErrorMatches(throwable -> {
                            if (throwable instanceof NotFoundEntity notFoundEntity) {
                                assertEquals(MODEL_NAME, notFoundEntity.getName());
                                assertEquals(1L, notFoundEntity.getId());
                                return true;
                            } else {
                                return false;
                            }
                        }
                ).verify();
    }

    @Test
    void getModelByIdWithUserBase_success() {
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(userClient.hasPermissionToModifyEntity(user1, model1.getUserId())).thenReturn(Mono.just(true));
        when(repository.findById(1L)).thenReturn(Mono.just(model1));
        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);
        var expected = new ResponseWithUserDto<>(bodyResp1, user1);
        StepVerifier.create(cacheWrapper.getModelByIdWithUserBase(user1, 1L))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void getModelByIdWithUserBase_throwsPrivateRoute() {
        when(userClient.hasPermissionToModifyEntity(user1, model2.getUserId())).thenReturn(Mono.just(false));
        when(repository.findById(1L)).thenReturn(Mono.just(model2));
        StepVerifier.create(cacheWrapper.getModelByIdWithUserBase(user1, 1L))
                .expectError(PrivateRouteException.class)
                .verify();
    }

    @Test
    void getModelByIdWithUserBase_throwsNotFound() {
        when(repository.findById(1L)).thenReturn(Mono.empty());
        StepVerifier.create(cacheWrapper.getModelByIdWithUserBase(user1, 1L))
                .expectErrorMatches(throwable -> {
                            if (throwable instanceof NotFoundEntity notFoundEntity) {
                                assertEquals(MODEL_NAME, notFoundEntity.getName());
                                assertEquals(1L, notFoundEntity.getId());
                                return true;
                            } else {
                                return false;
                            }
                        }
                ).verify();
    }

    @Test
    void getModelGroupedByMonthBase_success() {
        int month = LocalDateTime.now().getMonthValue();
        int year = LocalDateTime.now().getYear();
        var models = List.of(model1, model2);
        when(repository.findModelByMonth(month, year))
                .thenReturn(Flux.fromIterable(models));
        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);
        when(mapper.fromModelToResponse(model2)).thenReturn(bodyResp2);
        StepVerifier.create(cacheWrapper.getModelGroupedByMonthBase(month, user1))
                .expectNextMatches(r1 ->
                        r1.getEntity().equals(bodyResp1)
                                && r1.getYear() == year && r1.getMonth() == month
                )
                .expectNextMatches(r2 ->
                        r2.getEntity().equals(bodyResp2)
                                && r2.getYear() == year && r2.getMonth() == month
                )
                .verifyComplete();

    }

    @Test
    void getModelGroupedByMonthBase_notAdminThrows() {
        int month = LocalDateTime.now().getMonthValue();
        StepVerifier.create(cacheWrapper.getModelGroupedByMonthBase(month, user2))
                .expectError(PrivateRouteException.class)
                .verify();

    }

      /*
            SERVICE TESTS
     */

    @Test
    void createModel() {
        var model1Copy = model1.clone();
        model1Copy.setUserId(null);
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(mapper.fromBodyToModel(bodyResp1)).thenReturn(model1Copy);
        when(repository.save(model1Copy)).thenAnswer(ans -> {
            var calledModel = ans.getArgument(0, ManyToOneUserImpl.class);
            assertEquals(user1.getId(), calledModel.getUserId());
            return Mono.just(calledModel);
        });
        when(mapper.fromModelToResponse(model1Copy)).thenReturn(bodyResp1);

        StepVerifier.create(service.createModel(bodyResp1, "1"))
                .expectNext(bodyResp1)
                .verifyComplete();
    }

    @Test
    void getModelByIdIn() {
        var ids = List.of(1L, 2L);
        var models = List.of(model1, model2);
        when(mockedCacheWrapper.findAllById(ids)).thenReturn(Flux.fromIterable(models));
        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);
        when(mapper.fromModelToResponse(model2)).thenReturn(bodyResp2);
        StepVerifier.create(service.getModelsByIdIn(ids))
                .expectNext(bodyResp1, bodyResp2)
                .verifyComplete();
    }

    @Test
    void getModelsByIdInPageable_success() {
        var pageableBody = PageableBody.builder()
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

        var ids = List.of(1L, 2L);
        var models = List.of(model1, model2);
        when(repository.findAllByIdIn(eq(ids), any(PageRequest.class)))
                .thenReturn(Flux.fromIterable(models));
        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);
        when(mapper.fromModelToResponse(model2)).thenReturn(bodyResp2);
        when(repository.countAllByIdIn(ids)).thenReturn(Mono.just(2L));
        StepVerifier.create(service.getModelsByIdInPageable(ids, pageableBody))
                .expectNextMatches(r1 -> r1.getContent().equals(bodyResp1) &&
                        r1.getPageInfo().getCurrentPage() == 0 &&
                        r1.getPageInfo().getPageSize() == 2
                )
                .expectNextMatches(r2 -> r2.getContent().equals(bodyResp2) &&
                        r2.getPageInfo().getCurrentPage() == 0 &&
                        r2.getPageInfo().getPageSize() == 2
                )
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void privateRoute(boolean guard) {
        when(mockedCacheWrapper.privateRouteBase(eq(guard), any(), any())).thenReturn(Mono.empty());
        StepVerifier.create(service.privateRoute(guard, UserDto.builder().build(), 1L))
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void isNotAuthor(boolean r) {
        when(mockedCacheWrapper.isNotAuthor(model1, user1)).thenReturn(Mono.just(r));
        StepVerifier.create(service.isNotAuthor(model1, user1))
                .expectNext(r)
                .verifyComplete();
    }

    @Test
    void getModel_success() {
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        StepVerifier.create(service.getModel(model1.getId()))
                .expectNext(model1)
                .verifyComplete();
    }

    @Test
    void getModel_notFound() {
        when(repository.findById(model1.getId())).thenReturn(Mono.empty());
        StepVerifier.create(service.getModel(model1.getId()))
                .expectErrorMatches(throwable -> AssertionTestUtils.assertNotFound(throwable,
                                MODEL_NAME,
                                model1
                        )
                ).verify();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getModelGuardWithUser(boolean guard) {
        when(mockedCacheWrapper.getModelGuardWithUserBase(user1, model1, guard)).thenReturn(Mono.empty());
        StepVerifier.create(service.getModelGuardWithUser(user1, model1, guard))
                .verifyComplete();
    }

    @Test
    void getModelsWithUser() {
        var ids = List.of(1L, 2L);
        var models = List.of(model1, model2);
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(mockedCacheWrapper.findAllById(ids)).thenReturn(Flux.fromIterable(models));
        when(mockedCacheWrapper.getModelGuardWithUserBase(user1, model1, false)).thenReturn(Mono.just(new ResponseWithUserDto<>(bodyResp1, user1)));
        when(mockedCacheWrapper.getModelGuardWithUserBase(user1, model2, false)).thenReturn(Mono.just(new ResponseWithUserDto<>(bodyResp2, user1)));
        StepVerifier.create(service.getModelsWithUser(ids, user1.getId().toString()))
                .expectNextMatches(r1 -> r1.getModel().equals(bodyResp1) && r1.getUser().equals(user1))
                .expectNextMatches(r2 -> r2.getModel().equals(bodyResp2) && r2.getUser().equals(user1))
                .verifyComplete();
    }

    @Test
    void getModelByIdWithUser() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(mockedCacheWrapper.getModelByIdWithUserBase(user1, model1.getId())).thenReturn(Mono.just(new ResponseWithUserDto<>(bodyResp1, user1)));
        StepVerifier.create(service.getModelByIdWithUser(model1.getId(), user1.getId().toString()))
                .expectNextMatches(r -> r.getModel().equals(bodyResp1) && r.getUser().equals(user1))
                .verifyComplete();
    }

    @Test
    void updateModelWithSuccessGeneral_success() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.isNotAuthor(model1, user1)).thenReturn(Mono.just(false));
        doNothing().when(rabbitMqUpdateDeleteService).sendUpdateMessage(any(ManyToOneUserImpl.class));
        AtomicBoolean cbCalled = new AtomicBoolean(false);
        Function<ManyToOneUserImpl, Mono<WithUserDtoImpl>> cb = m -> {
            assertEquals(model1.getId(), m.getId());
            cbCalled.set(true);
            return Mono.just(bodyResp1);
        };
        StepVerifier.create(Objects.requireNonNull(
                        ReflectionTestUtils.invokeMethod(service, "updateModelWithSuccessGeneral", model1.getId(), user1.getId().toString(), cb)
                ))
                .expectNextMatches(r -> {
                    assertTrue(cbCalled.get());
                    return r.equals(bodyResp1);
                })
                .verifyComplete();
    }

    @Test
    void updateModelWithSuccessGeneral_throwsPrivateRoute() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.isNotAuthor(model1, user1)).thenReturn(Mono.just(true));
        AtomicBoolean cbCalled = new AtomicBoolean(false);
        Function<ManyToOneUserImpl, Mono<WithUserDtoImpl>> cb = m -> {
            assertEquals(model1.getId(), m.getId());
            cbCalled.set(true);
            return Mono.just(bodyResp1);
        };
        StepVerifier.create(Objects.requireNonNull(
                        ReflectionTestUtils.invokeMethod(service, "updateModelWithSuccessGeneral", model1.getId(), user1.getId().toString(), cb)
                ))
                .expectError(PrivateRouteException.class)
                .verify();

        assertFalse(cbCalled.get());
    }

    @Test
    void updateModelWithSuccess_success() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.isNotAuthor(model1, user1)).thenReturn(Mono.just(false));
        doNothing().when(rabbitMqUpdateDeleteService).sendUpdateMessage(any(ManyToOneUserImpl.class));
        AtomicBoolean cbCalled = new AtomicBoolean(false);
        Function<ManyToOneUserImpl, Mono<ManyToOneUserImpl>> cb = m -> {
            assertEquals(model1.getId(), m.getId());
            cbCalled.set(true);
            return Mono.just(m);
        };
        when(repository.save(model1)).thenReturn(Mono.just(model1));
        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);
        StepVerifier.create(Objects.requireNonNull(
                        ReflectionTestUtils.invokeMethod(service, "updateModelWithSuccess", model1.getId(), user1.getId().toString(), cb)
                ))
                .expectNext(bodyResp1)
                .verifyComplete();
        assertTrue(cbCalled.get());
    }

    @Test
    void updateModelWithSuccess_throwsPrivateRoute() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.isNotAuthor(model1, user1)).thenReturn(Mono.just(true));
        AtomicBoolean cbCalled = new AtomicBoolean(false);
        Function<ManyToOneUserImpl, Mono<ManyToOneUserImpl>> cb = m -> {
            assertEquals(model1.getId(), m.getId());
            cbCalled.set(true);
            return Mono.just(m);
        };
        StepVerifier.create(Objects.requireNonNull(
                        ReflectionTestUtils.invokeMethod(service, "updateModelWithSuccess", model1.getId(), user1.getId().toString(), cb)
                ))
                .expectError(PrivateRouteException.class)
                .verify();

        assertFalse(cbCalled.get());
    }

    @Test
    void updateModel_success() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.isNotAuthor(model1, user1)).thenReturn(Mono.just(false));
        doNothing().when(rabbitMqUpdateDeleteService).sendUpdateMessage(any(ManyToOneUserImpl.class));
        when(repository.save(model1)).thenReturn(Mono.just(model1));
        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);
        when(mapper.updateModelFromBody(bodyResp1, model1)).thenReturn(Mono.just(model1));
        StepVerifier.create(service.updateModel(model1.getId(), bodyResp1, user1.getId().toString()))
                .expectNext(bodyResp1)
                .verifyComplete();
    }

    @Test
    void getAllModels() {
        var pageableBody = PageableBody.builder()
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
        var models = List.of(model1, model2);
        when(repository.findAllBy(any(PageRequest.class)))
                .thenReturn(Flux.fromIterable(models));
        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);
        when(mapper.fromModelToResponse(model2)).thenReturn(bodyResp2);
        when(repository.count()).thenReturn(Mono.just(2L));
        StepVerifier.create(service.getAllModels(pageableBody, "1"))
                .expectNextMatches(r1 -> r1.getContent().equals(bodyResp1) &&
                        r1.getPageInfo().getCurrentPage() == 0 &&
                        r1.getPageInfo().getPageSize() == 2
                )
                .expectNextMatches(r2 -> r2.getContent().equals(bodyResp2) &&
                        r2.getPageInfo().getCurrentPage() == 0 &&
                        r2.getPageInfo().getPageSize() == 2
                )
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getResponseGuard_success(boolean guard) {
        when(mockedCacheWrapper.privateRouteBase(eq(guard), any(), any())).thenReturn(Mono.empty());
        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);
        StepVerifier.create(service.getResponseGuard(user1, model1, guard))
                .expectNext(bodyResp1)
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getResponseGuard_throwsPrivateRoute(boolean guard) {
        when(mockedCacheWrapper.privateRouteBase(eq(guard), any(), any())).thenReturn(Mono.error(new PrivateRouteException()));
        StepVerifier.create(service.getResponseGuard(user1, model1, guard))
                .expectError(PrivateRouteException.class)
                .verify();
    }

    @Test
    void getModelById() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(mockedCacheWrapper.getModel(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.privateRouteBase(true, user1, model1.getUserId())).thenReturn(Mono.empty());
        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);

        StepVerifier.create(service.getModelById(model1.getId(), user1.getId().toString()))
                .expectNext(bodyResp1)
                .verifyComplete();
    }

    @Test
    void getModelById_throwsNotFound() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(mockedCacheWrapper.getModel(model1.getId())).thenReturn(Mono.error(new NotFoundEntity(MODEL_NAME, model1.getId())));
        StepVerifier.create(service.getModelById(model1.getId(), user1.getId().toString()))
                .expectErrorMatches(throwable -> AssertionTestUtils.assertNotFound(throwable,
                                MODEL_NAME,
                                model1
                        )
                ).verify();
    }

    @Test
    void getModelById_throwsPrivateRoute() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(mockedCacheWrapper.getModel(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.privateRouteBase(true, user1, model1.getUserId())).thenReturn(Mono.error(new PrivateRouteException()));
        StepVerifier.create(service.getModelById(model1.getId(), user1.getId().toString()))
                .expectError(PrivateRouteException.class)
                .verify();
    }

    @Test
    void deleteModel_success() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.privateRouteBase(true, user1, model1.getUserId())).thenReturn(Mono.empty());
        doNothing().when(rabbitMqUpdateDeleteService).sendDeleteMessage(any(ManyToOneUserImpl.class));
        when(repository.delete(model1)).thenReturn(Mono.empty());
        StepVerifier.create(service.deleteModel(model1.getId(), user1.getId().toString()))
                .verifyComplete();
    }

    @Test
    void deleteModel_throwsNotFound() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model1.getId())).thenReturn(Mono.empty());
        StepVerifier.create(service.deleteModel(model1.getId(), user1.getId().toString()))
                .expectErrorMatches(throwable ->
                        AssertionTestUtils.assertNotFound(throwable, MODEL_NAME, model1)
                ).verify();
    }

    @Test
    void deleteModel_throwsPrivateRoute() {
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(mockedCacheWrapper.privateRouteBase(true, user1, model1.getUserId())).thenReturn(Mono.error(new PrivateRouteException()));
        StepVerifier.create(service.deleteModel(model1.getId(), user1.getId().toString()))
                .expectError(PrivateRouteException.class)
                .verify();
    }

    @Test
    void toResponseWithCount_success() {
        var pageInfo = PageInfo.builder()
                .currentPage(0)
                .pageSize(1)
                .totalElements(1)
                .totalPages(1)
                .build();
        List<Link> links = List.of();
        var pr =
                PageableResponse.<WithUserDtoImpl>builder()
                        .content(bodyResp1)
                        .pageInfo(pageInfo)
                        .links(links)
                        .build();
        var ec = EntityCount.builder().count(1L).build();
        PageableResponse<ResponseWithEntityCount<WithUserDtoImpl>> expected = PageableResponse.<ResponseWithEntityCount<WithUserDtoImpl>>builder()
                .content(ResponseWithEntityCount.of(bodyResp1, ec))
                .pageInfo(pageInfo)
                .links(links)
                .build();
        when(countInParentClient.getCountInParent(pr.getContent().getId(), user1.getId().toString())).thenReturn(Mono.just(ec));
        StepVerifier.create(
                        Objects.requireNonNull(
                                ReflectionTestUtils.invokeMethod(service, "toResponseWithCount", user1.getId().toString(), countInParentClient, pr)
                        )
                )
                .expectNextMatches(r -> {

                    if (r instanceof PageableResponse<?> pageableResponse) {
                        assertEquals(pageableResponse.getContent(), expected.getContent());
                        assertEquals(pageableResponse.getPageInfo(), expected.getPageInfo());
                        assertEquals(pageableResponse.getLinks(), expected.getLinks());
                        return true;
                    }
                    return false;
                })
                .verifyComplete();
    }

    @Test
    void getPageableWithUser_success() {
        var pageInfo = PageInfo.builder()
                .currentPage(0)
                .pageSize(1)
                .totalElements(1)
                .totalPages(1)
                .build();
        List<Link> links = List.of();
        var pr =
                PageableResponse.<WithUserDtoImpl>builder()
                        .content(bodyResp1)
                        .pageInfo(pageInfo)
                        .links(links)
                        .build();
        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        var expected = PageableResponse.<ResponseWithUserDto<WithUserDtoImpl>>builder()
                .content(new ResponseWithUserDto<>(bodyResp1, user1))
                .pageInfo(pageInfo)
                .links(links)
                .build();
        StepVerifier.create(service.getPageableWithUser(pr))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void getModelGroupedByMonth() {
        int month = LocalDateTime.now().getMonthValue();
        int year = LocalDateTime.now().getYear();
        var bodies = List.of(bodyResp1, bodyResp2);
        var expected = bodies.stream().map(b -> MonthlyEntityGroup.<WithUserDtoImpl>builder()
                .entity(b)
                .year(year)
                .month(month)
                .build()).toList();

        when(userClient.getUser("", user1.getId().toString())).thenReturn(Mono.just(user1));
        when(mockedCacheWrapper.getModelGroupedByMonthBase(month, user1)).thenReturn(Flux.fromIterable(expected));

        StepVerifier.create(service.getModelGroupedByMonth(month, user1.getId().toString()))
                .expectNextSequence(expected)
                .verifyComplete();
    }


}