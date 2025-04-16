package com.mocicarazvan.templatemodule.controllers.beans;

import com.mocicarazvan.templatemodule.controllers.ManyToOneUserController;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.WithUserDtoImpl;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import com.mocicarazvan.templatemodule.hateos.controller.generics.ManyToOneUserReactiveLinkBuilder;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.models.ManyToOneUserImpl;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import com.mocicarazvan.templatemodule.services.ManyToOneUserService;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/manyToOneUser")
public class ManyToOneUserControllerBeanTest implements
        ManyToOneUserController<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl,
                ManyToOneUserRepository<ManyToOneUserImpl>, DtoMapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl>,
                ManyToOneUserService<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl, ManyToOneUserRepository<ManyToOneUserImpl>,
                        DtoMapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl>>> {

    private final ManyToOneUserService<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl,
            ManyToOneUserRepository<ManyToOneUserImpl>, DtoMapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl>> service;

    private final ManyToOneLinkBuilder responseBuilder;
    private final RequestsUtils requestsUtils;

    public ManyToOneUserControllerBeanTest(ManyToOneUserService<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl, ManyToOneUserRepository<ManyToOneUserImpl>, DtoMapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl>> service, ManyToOneLinkBuilder responseBuilder, RequestsUtils requestsUtils) {
        this.service = service;
        this.responseBuilder = responseBuilder;
        this.requestsUtils = requestsUtils;
    }

    @Override
    @DeleteMapping(value = "/delete/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<WithUserDtoImpl>>> deleteModel(@PathVariable Long id, ServerWebExchange exchange) {
        return service.deleteModel(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> responseBuilder.toModel(m, ManyToOneUserControllerBeanTest.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<WithUserDtoImpl>>> getModelById(@PathVariable Long id, ServerWebExchange exchange) {
        return service.getModelById(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> responseBuilder.toModel(m, ManyToOneUserControllerBeanTest.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/withUser/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<WithUserDtoImpl>>> getModelByIdWithUser(@PathVariable Long id, ServerWebExchange exchange) {
        return service.getModelByIdWithUser(id, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> responseBuilder.toModelWithUser(m, ManyToOneUserControllerBeanTest.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PutMapping(value = "/update/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<WithUserDtoImpl>>> updateModel(@Valid @RequestBody WithUserDtoImpl withUserDto, @PathVariable Long id, ServerWebExchange exchange) {
        return service.updateModel(id, withUserDto, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> responseBuilder.toModel(m, ManyToOneUserControllerBeanTest.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @PatchMapping(value = "/byIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<CustomEntityModel<WithUserDtoImpl>>> getModelsByIdIn(@Valid @RequestBody PageableBody pageableBody,
                                                                                      @RequestParam List<Long> ids) {
        return service.getModelsByIdInPageable(ids, pageableBody)
                .flatMap(m -> responseBuilder.toModelPageable(m, ManyToOneUserControllerBeanTest.class));
    }

    @Override
    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<WithUserDtoImpl>>> createModel(@Valid @RequestBody WithUserDtoImpl withUserDto, ServerWebExchange exchange) {
        return service.createModel(withUserDto, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> responseBuilder.toModel(m, ManyToOneUserControllerBeanTest.class))
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/admin/groupedByMonth", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyEntityGroup<CustomEntityModel<WithUserDtoImpl>>> getModelGroupedByMonth(@RequestParam int month, ServerWebExchange exchange) {
        return service.getModelGroupedByMonth(month, requestsUtils.extractAuthUser(exchange))
                .flatMap(m -> responseBuilder.toModelMonthlyEntityGroup(m, ManyToOneUserControllerBeanTest.class));
    }

    public static class MTORLB extends ManyToOneUserReactiveLinkBuilder<ManyToOneUserImpl,
            WithUserDtoImpl, WithUserDtoImpl, ManyToOneUserRepository<ManyToOneUserImpl>,
            DtoMapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl>,
            ManyToOneUserService<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl,
                    ManyToOneUserRepository<ManyToOneUserImpl>, DtoMapper<ManyToOneUserImpl, WithUserDtoImpl, WithUserDtoImpl>>,
            ManyToOneUserControllerBeanTest
            > {
    }

    public static class ManyToOneLinkBuilder extends ReactiveResponseBuilder<WithUserDtoImpl, ManyToOneUserControllerBeanTest> {

        public ManyToOneLinkBuilder() {
            super(new MTORLB());
        }
    }
}
