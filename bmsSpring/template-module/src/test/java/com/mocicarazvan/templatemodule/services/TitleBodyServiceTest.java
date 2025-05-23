package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.WithUserDtoImpl;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserLikesAndDislikes;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.TitleBodyImpl;
import com.mocicarazvan.templatemodule.repositories.AssociativeEntityRepository;
import com.mocicarazvan.templatemodule.repositories.TitleBodyRepository;
import com.mocicarazvan.templatemodule.services.impl.TitleBodyServiceImpl;
import com.mocicarazvan.templatemodule.testUtils.AssertionTestUtils;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TitleBodyServiceTest {

    @Mock
    TitleBodyRepository<TitleBodyImpl> repository;

    @Mock
    DtoMapper<TitleBodyImpl, WithUserDtoImpl, WithUserDtoImpl> mapper;

    @Mock
    UserClient userClient;

    @Spy
    PageableUtilsCustom pageableUtils;

    @Mock
    RabbitMqUpdateDeleteService<TitleBodyImpl> rabbitMqUpdateDeleteService;

    @Spy
    EntitiesUtils entitiesUtils;

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

    private final TitleBodyImpl model1 = TitleBodyImpl.builder()
            .id(1L)
            .userId(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .title("Title 1")
            .body("Body 1")
            .userLikes(List.of(3L))
            .userDislikes(List.of(4L))
            .build();

    private final TitleBodyImpl model2 = TitleBodyImpl.builder()
            .id(2L)
            .userId(2L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .title("Title 2")
            .body("Body 2")
            .userLikes(List.of(4L))
            .userDislikes(List.of(3L))
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

    private TitleBodyServiceImpl.TitleBodyServiceRedisCacheWrapper<TitleBodyImpl, WithUserDtoImpl, WithUserDtoImpl,
            TitleBodyRepository<TitleBodyImpl>, DtoMapper<TitleBodyImpl, WithUserDtoImpl, WithUserDtoImpl>
            > cacheWrapper;

    @Mock
    private TitleBodyServiceImpl.TitleBodyServiceRedisCacheWrapper<TitleBodyImpl, WithUserDtoImpl, WithUserDtoImpl,
            TitleBodyRepository<TitleBodyImpl>, DtoMapper<TitleBodyImpl, WithUserDtoImpl, WithUserDtoImpl>
            > mockedCacheWrapper;

    private TitleBodyServiceImpl<TitleBodyImpl, WithUserDtoImpl, WithUserDtoImpl,
            TitleBodyRepository<TitleBodyImpl>, DtoMapper<TitleBodyImpl, WithUserDtoImpl, WithUserDtoImpl>,
            TitleBodyServiceImpl.TitleBodyServiceRedisCacheWrapper<TitleBodyImpl, WithUserDtoImpl, WithUserDtoImpl,
                    TitleBodyRepository<TitleBodyImpl>, DtoMapper<TitleBodyImpl, WithUserDtoImpl, WithUserDtoImpl>
                    >
            > service;

    @Mock
    private AssociativeEntityRepository userLikesRepository;

    @Mock
    private AssociativeEntityRepository userDislikesRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    private static final String MODEL_NAME = "titleBody";
    private static final List<String> ALLOWED_SORTING_FIELDS = List.of("createdAt", "updatedAt", "id", "title", "body");

    @BeforeEach
    void setUp() {
        cacheWrapper = new TitleBodyServiceImpl.TitleBodyServiceRedisCacheWrapper<>(
                repository, mapper, MODEL_NAME, userClient
        );
        service = new TitleBodyServiceImpl<>(
                repository, mapper, pageableUtils, userClient, MODEL_NAME, ALLOWED_SORTING_FIELDS, entitiesUtils, mockedCacheWrapper,
                rabbitMqUpdateDeleteService, transactionalOperator, userLikesRepository, userDislikesRepository
        ) {

            @Override
            public TitleBodyImpl cloneModel(TitleBodyImpl model) {
                return model.clone();
            }
        };
    }

    @Test
    @Order(1)
    void loads() {
        assertNotNull(service);
        assertNotNull(repository);
        assertNotNull(mapper);
        assertNotNull(userClient);
        assertNotNull(pageableUtils);
        assertNotNull(entitiesUtils);
        assertNotNull(rabbitMqUpdateDeleteService);
    }

    /*
            CACHE WRAPPER TESTS
     */

    @Test
    void getModelGuardWithLikesAndDislikesBase_noGuard_success() {
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(userClient.getUsersByIdIn("/byIds", model1.getUserLikes())).thenReturn(Flux.just(user3));
        when(userClient.getUsersByIdIn("/byIds", model1.getUserDislikes())).thenReturn(Flux.just(user4));

        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);

        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);

        var expected = ResponseWithUserLikesAndDislikes.<WithUserDtoImpl>builder()
                .model(bodyResp1)
                .userLikes(List.of(user3))
                .userDislikes(List.of(user4))
                .user(user1)
                .build();

        StepVerifier.create(cacheWrapper.getModelGuardWithLikesAndDislikesBase(user1, model1, false))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void getModelGuardWithLikesAndDislikesBase_guard_success() {
        when(userClient.hasPermissionToModifyEntity(user1, model1.getUserId())).thenReturn(Mono.just(true));
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(userClient.getUsersByIdIn("/byIds", model1.getUserLikes())).thenReturn(Flux.just(user3));
        when(userClient.getUsersByIdIn("/byIds", model1.getUserDislikes())).thenReturn(Flux.just(user4));

        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);

        var expected = ResponseWithUserLikesAndDislikes.<WithUserDtoImpl>builder()
                .model(bodyResp1)
                .userLikes(List.of(user3))
                .userDislikes(List.of(user4))
                .user(user1)
                .build();

        StepVerifier.create(cacheWrapper.getModelGuardWithLikesAndDislikesBase(user1, model1, true))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void getModelGuardWithLikesAndDislikesBase_guard_throwsPrivateRoute() {
        when(userClient.hasPermissionToModifyEntity(user2, model1.getUserId())).thenReturn(Mono.just(false));


        StepVerifier.create(cacheWrapper.getModelGuardWithLikesAndDislikesBase(user2, model1, true))
                .expectError(PrivateRouteException.class)
                .verify();
    }

    @Test
    void reactToModelInvalidate() {
        StepVerifier.create(cacheWrapper.reactToModelInvalidate(bodyResp1))
                .expectNext(bodyResp1)
                .verifyComplete();
    }

    @Test
    void getModelByIdWithUserLikesAndDislikesBase_success() {
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(userClient.hasPermissionToModifyEntity(user1, model1.getUserId())).thenReturn(Mono.just(true));
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(userClient.getUsersByIdIn("/byIds", model1.getUserLikes())).thenReturn(Flux.just(user3));
        when(userClient.getUsersByIdIn("/byIds", model1.getUserDislikes())).thenReturn(Flux.just(user4));

        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);

        var expected = ResponseWithUserLikesAndDislikes.<WithUserDtoImpl>builder()
                .model(bodyResp1)
                .userLikes(List.of(user3))
                .userDislikes(List.of(user4))
                .user(user1)
                .build();

        StepVerifier.create(cacheWrapper.getModelByIdWithUserLikesAndDislikesBase(model1.getId(), user1))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void getModelByIdWithUserLikesAndDislikesBase_noFound() {
        when(repository.findById(model1.getId())).thenReturn(Mono.empty());

        StepVerifier.create(cacheWrapper.getModelByIdWithUserLikesAndDislikesBase(model1.getId(), user1))
                .expectErrorMatches(t -> AssertionTestUtils.assertNotFound(t, MODEL_NAME, model1))
                .verify();
    }

    @Test
    void getModelByIdWithUserLikesAndDislikesBase_throwsPrivateRoute() {
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(userClient.hasPermissionToModifyEntity(user2, model1.getUserId())).thenReturn(Mono.just(false));


        StepVerifier.create(cacheWrapper.getModelByIdWithUserLikesAndDislikesBase(model1.getId(), user2))
                .expectError(PrivateRouteException.class)
                .verify();
    }


    /*
          SERVICE TESTS
   */
    @Test
    void createModel_notNullLikesDislikes() {
        var model = model1.clone();
        model.setUserDislikes(List.of());
        model.setUserLikes(List.of());
        model.setUserId(null);

        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        ArgumentCaptor<TitleBodyImpl> captor = ArgumentCaptor.forClass(TitleBodyImpl.class);

        when(repository.save(any(TitleBodyImpl.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mapper.fromBodyToModel(bodyResp1)).thenReturn(model);
        when(repository.save(model)).thenReturn(Mono.just(model));
        when(mapper.fromModelToResponse(model)).thenReturn(bodyResp1);

        StepVerifier.create(service.createModel(bodyResp1, user1.getId().toString()))
                .expectNext(bodyResp1)
                .verifyComplete();
        verify(repository).save(captor.capture());
        TitleBodyImpl m = captor.getValue();

        assertEquals(List.of(), m.getUserLikes());
        assertEquals(List.of(), m.getUserDislikes());
        assertEquals(user1.getId(), m.getUserId());
    }

    @Test
    void createModel_nullLikesDislikes() {
        var model = model1.clone();
        model.setUserDislikes(null);
        model.setUserLikes(null);
        model.setUserId(null);

        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        ArgumentCaptor<TitleBodyImpl> captor = ArgumentCaptor.forClass(TitleBodyImpl.class);

        when(repository.save(any(TitleBodyImpl.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mapper.fromBodyToModel(bodyResp1)).thenReturn(model);
        when(repository.save(model)).thenReturn(Mono.just(model));
        when(mapper.fromModelToResponse(model)).thenReturn(bodyResp1);

        StepVerifier.create(service.createModel(bodyResp1, user1.getId().toString()))
                .expectNext(bodyResp1)
                .verifyComplete();
        verify(repository).save(captor.capture());
        TitleBodyImpl m = captor.getValue();

        assertEquals(List.of(), m.getUserLikes());
        assertEquals(List.of(), m.getUserDislikes());
        assertEquals(user1.getId(), m.getUserId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"like", "dislike"})
    void reactModel_success(String type) {
        when(repository.findById(model1.getId())).thenReturn(Mono.just(model1));
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(entitiesUtils.setReaction(model1, user1, type, userLikesRepository, userDislikesRepository)).thenReturn(Mono.just(model1));
        when(repository.save(model1)).thenReturn(Mono.just(model1));
        when(mapper.fromModelToResponse(model1)).thenReturn(bodyResp1);
        when(mockedCacheWrapper.reactToModelInvalidate(bodyResp1)).thenReturn(Mono.just(bodyResp1));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0, Mono.class));

        StepVerifier.create(service.reactToModel(model1.getId(), type, user1.getId().toString()))
                .expectNext(bodyResp1)
                .verifyComplete();
    }

    @Test
    void reactModel_throwsNotFound() {
        when(userClient.getUser("", "1")).thenReturn(Mono.just(user1));
        when(repository.findById(model1.getId())).thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0, Mono.class));

        StepVerifier.create(service.reactToModel(model1.getId(), "like", user1.getId().toString()))
                .expectErrorMatches(t -> AssertionTestUtils.assertNotFound(t, MODEL_NAME, model1))
                .verify();
    }
}