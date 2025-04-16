package com.mocicarazvan.templatemodule.controllers;

import com.mocicarazvan.templatemodule.config.ManyToOneUserControllerTestConfig;
import com.mocicarazvan.templatemodule.controllers.beans.ControllerAdviceBeanTest;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.WithUserDtoImpl;
import com.mocicarazvan.templatemodule.dtos.errors.*;
import com.mocicarazvan.templatemodule.dtos.response.PageInfo;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityNotOwner;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.exceptions.common.ServiceCallFailedException;
import com.mocicarazvan.templatemodule.exceptions.common.SortingCriteriaException;
import com.mocicarazvan.templatemodule.exceptions.common.UsernameNotFoundException;
import com.mocicarazvan.templatemodule.exceptions.notFound.AuthHeaderNotFound;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.ManyToOneUserImpl;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import com.mocicarazvan.templatemodule.services.ManyToOneUserService;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ManyToOneUserController.class)
@Import({ManyToOneUserControllerTestConfig.class, ControllerAdviceBeanTest.class})
@Execution(ExecutionMode.SAME_THREAD)
class ManyToOneUserControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private ManyToOneUserService<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl,
            ManyToOneUserRepository<ManyToOneUserImpl>, DtoMapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl>> service;

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

    private final PageInfo pageInfo = PageInfo.builder()
            .totalPages(1)
            .totalElements(2)
            .currentPage(0)
            .pageSize(10)
            .build();

    private final PageableBody pageableBody = PageableBody.builder().build();

    @Test
    void getById_success() {

        when(service.getModelById(1L, "1"))
                .thenReturn(Mono.just(bodyResp1));
        webTestClient.get()
                .uri("/manyToOneUser/1")
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<CustomEntityModel<WithUserDtoImpl>>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(bodyResp1, body.getContent());
                    assertNotNull(body.get_links());
                });
    }

    private static final String MODEL_NAME = "manyToOneUser";

    @Test
    void getById_notFound() {
        var ex = new NotFoundEntity(MODEL_NAME, 1L);
        when(service.getModelById(1L, "1"))
                .thenReturn(Mono.error(ex));
        webTestClient.get()
                .uri("/manyToOneUser/1")
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(new ParameterizedTypeReference<BaseErrorResponse>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(ex.getMessage(), body.getMessage());
                    assertEquals(404, body.getStatus());
                });
    }

    @Test
    void getById_privateRoute() {
        var ex = new PrivateRouteException();
        when(service.getModelById(1L, "1"))
                .thenReturn(Mono.error(ex));
        webTestClient.get()
                .uri("/manyToOneUser/1")
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(new ParameterizedTypeReference<BaseErrorResponse>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(ex.getMessage(), body.getMessage());
                    assertEquals(403, body.getStatus());
                });
    }


    @Test
    void getById_headerNotPresent() {
        var ex = new AuthHeaderNotFound();
        webTestClient.get()
                .uri("/manyToOneUser/1")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(new ParameterizedTypeReference<BaseErrorResponse>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(ex.getMessage(), body.getMessage());
                    assertEquals(404, body.getStatus());
                });
    }

    @Test
    void deleteModel_success() {
        when(service.deleteModel(1L, "1"))
                .thenReturn(Mono.just(bodyResp1));
        webTestClient.delete()
                .uri("/manyToOneUser/delete/1")
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<CustomEntityModel<WithUserDtoImpl>>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(bodyResp1, body.getContent());
                    assertNotNull(body.get_links());
                });
    }

    @Test
    void deleteModel_subEntityNotOwner() {
        var ex = new SubEntityNotOwner(1L, 2L, 2L);
        when(service.deleteModel(1L, "1"))
                .thenReturn(Mono.error(ex));
        webTestClient.delete()
                .uri("/manyToOneUser/delete/1")
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(new ParameterizedTypeReference<SubEntityOwnerResponse>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(ex.getAuthId(), body.getReceivedUserId());
                    assertEquals(ex.getEntityUserId(), body.getExpectedUserId());
                    assertEquals(ex.getEntityId(), body.getEntityId());
                    assertEquals(ex.getMessage(), body.getMessage());
                    assertEquals(403, body.getStatus());
                });
    }

    @Test
    void deleteModel_subEntityUsed() {
        var ex = new SubEntityUsed("manyToOneUser", 1L);
        when(service.deleteModel(1L, "1"))
                .thenReturn(Mono.error(ex));
        webTestClient.delete()
                .uri("/manyToOneUser/delete/1")
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(new ParameterizedTypeReference<IdNameResponse>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals("manyToOneUser", body.getName());
                    assertEquals(1L, body.getId());
                    assertEquals(ex.getMessage(), body.getMessage());
                    assertEquals(400, body.getStatus());
                });
    }

    @Test
    void getModelByIdWithUser_success() {
        var resp = new ResponseWithUserDto<>(bodyResp1, user1);
        when(service.getModelByIdWithUser(1L, "1"))
                .thenReturn(Mono.just(resp));

        webTestClient.get()
                .uri("/manyToOneUser/withUser/1")
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseWithUserDtoEntity<WithUserDtoImpl>>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(resp.getModel(), body.getModel().getContent());
                    assertEquals(resp.getUser(), body.getUser());
                    assertNotNull(body.getModel().get_links());
                });
    }

    @Test
    void getModelByIdWithUser_usernameNotFoundException() {
        var ex = new UsernameNotFoundException("User not found");
        when(service.getModelByIdWithUser(1L, "1"))
                .thenReturn(Mono.error(ex));


        webTestClient.get()
                .uri("/manyToOneUser/withUser/1")
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(new ParameterizedTypeReference<BaseErrorResponse>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(ex.getMessage(), body.getMessage());
                    assertEquals(404, body.getStatus());
                });
    }

    @Test
    void getModelByIdWithUser_serviceCalledFailedException() {
        var ex = new ServiceCallFailedException("Service call failed", "user", "/manyToOneUser/withUser/1");
        when(service.getModelByIdWithUser(1L, "1"))
                .thenReturn(Mono.error(ex));

        webTestClient.get()
                .uri("/manyToOneUser/withUser/1")
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody(new ParameterizedTypeReference<ServiceCallFailedExceptionResponse>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(ex.getServiceName(), body.getServiceName());
                    assertEquals(ex.getMessage(), body.getMessage());
                    assertEquals(ex.getServicePath(), body.getServicePath());
                    assertEquals(500, body.getStatus());
                });
    }

    @Test
    void updateModel_success() {
        when(service.updateModel(1L, bodyResp1, "1"))
                .thenReturn(Mono.just(bodyResp1));
        webTestClient.put()
                .uri("/manyToOneUser/update/1")
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .bodyValue(bodyResp1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<CustomEntityModel<WithUserDtoImpl>>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(bodyResp1, body.getContent());
                    assertNotNull(body.get_links());
                });
    }

    @Test
    void updateModel_illegalActionException() {
        var ex = new IllegalActionException("Illegal action");
        when(service.updateModel(1L, bodyResp1, "1"))
                .thenReturn(Mono.error(ex));
        webTestClient.put()
                .uri("/manyToOneUser/update/1")
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .bodyValue(bodyResp1)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(new ParameterizedTypeReference<BaseErrorResponse>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(ex.getMessage(), body.getMessage());
                    assertEquals(400, body.getStatus());
                });
    }

    @Test
    void getModelsByIdIn_success() {
        var ids = List.of(1L, 2L);
        var resp = Flux.just(bodyResp1, bodyResp2)
                .map(r -> PageableResponse.<WithUserDtoImpl>builder()
                        .pageInfo(pageInfo)
                        .content(r)
                        .build());
        when(service.getModelsByIdInPageable(ids, pageableBody))
                .thenReturn(resp);

        webTestClient.patch()
                .uri(uriBuilder ->
                        uriBuilder.path("/manyToOneUser/byIds")
                                .queryParam("ids", ids)
                                .build()
                )
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .bodyValue(pageableBody)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(new ParameterizedTypeReference<PageableResponse<CustomEntityModel<WithUserDtoImpl>>>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(2, body.size());
                    assertEquals(bodyResp1, body.get(0).getContent().getContent());
                    assertEquals(bodyResp2, body.get(1).getContent().getContent());
                    assertEquals(pageInfo, body.get(0).getPageInfo());
                    assertEquals(pageInfo, body.get(1).getPageInfo());
                    assertNotNull(body.get(0).getLinks());
                    assertNotNull(body.get(1).getLinks());


                });
    }

    @Test
    void getModelsByIdIn_bodyNotValid() {
        var ids = List.of(1L, 2L);
        var notValidBody = PageableBody.builder()
                .page(-1)
                .size(-1)
                .build();

        webTestClient.patch()
                .uri(uriBuilder ->
                        uriBuilder.path("/manyToOneUser/byIds")
                                .queryParam("ids", ids)
                                .build()
                )
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .bodyValue(notValidBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(new ParameterizedTypeReference<ValidationResponse>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(2, body.reasons.size());
                    assertTrue(body.reasons.containsKey("page"));
                    assertTrue(body.reasons.containsKey("size"));
                    assertEquals(400, body.getStatus());
                });
    }

    @Test
    void getModelsByIdIn_sortingNotValid() {
        var ids = List.of(1L, 2L);
        var ex = new SortingCriteriaException("Invalid sorting criteria provided.", Map.of("name", "inv"));
        when(service.getModelsByIdInPageable(ids, pageableBody))
                .thenReturn(Flux.error(ex));
        webTestClient.patch()
                .uri(uriBuilder ->
                        uriBuilder.path("/manyToOneUser/byIds")
                                .queryParam("ids", ids)
                                .build()
                )
                .accept(MediaType.APPLICATION_NDJSON)
                .header(RequestsUtils.AUTH_HEADER, "1")
                .bodyValue(pageableBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(new ParameterizedTypeReference<SortingCriteriaResponse>() {
                })
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertNotNull(body);
                    assertEquals(1, body.getSortingCriteria().size());
                    assertTrue(body.getSortingCriteria().containsKey("name"));
                    assertEquals("inv", body.getSortingCriteria().get("name"));
                    assertEquals(400, body.getStatus());
                });
    }


}