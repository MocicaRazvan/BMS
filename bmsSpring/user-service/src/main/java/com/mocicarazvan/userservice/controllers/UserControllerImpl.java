package com.mocicarazvan.userservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.controllers.UserController;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.email.EmailRequest;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.hateos.user.PageableUserAssembler;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import com.mocicarazvan.userservice.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserControllerImpl implements UserController {

    private final UserService userService;
    private final PageableUserAssembler pageableUserAssembler;
    private final RequestsUtils requestsUtils;
    private final ObjectMapper objectMapper;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    public UserControllerImpl(UserService userService, PageableUserAssembler pageableUserAssembler,
                              RequestsUtils requestsUtils, ObjectMapper objectMapper,
                              @Qualifier("threadPoolTaskScheduler") ThreadPoolTaskScheduler threadPoolTaskScheduler
    ) {
        this.userService = userService;
        this.pageableUserAssembler = pageableUserAssembler;
        this.requestsUtils = requestsUtils;
        this.objectMapper = objectMapper;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }


    @Override
    @GetMapping(value = "/roles", produces = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_NDJSON_VALUE})
    public Mono<ResponseEntity<List<Role>>> getRoles() {
        return Mono.just(ResponseEntity.ok(List.of(Role.values())));
    }

    @Override
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<UserDto>>> getUser(@PathVariable Long id) {
        return userService.getUser(id)
                .flatMap(u -> pageableUserAssembler.getItemAssembler().toModel(u))
                .map(ResponseEntity::ok);

    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<PageableResponse<CustomEntityModel<UserDto>>> getAllUsers(@Valid @RequestBody PageableBody pageableBody,
                                                                          @RequestParam(required = false) String email,
                                                                          @RequestParam(required = false) Set<Role> roles,
                                                                          @RequestParam(required = false) Set<AuthProvider> providers,
                                                                          @RequestParam(required = false) Boolean emailVerified,
                                                                          @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin,
                                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtLowerBound,
                                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate createdAtUpperBound,
                                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtLowerBound,
                                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate updatedAtUpperBound
    ) {
        return userService.getAllUsers(pageableBody, email, roles, providers, emailVerified, admin, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound)
                .flatMapSequential(pageableUserAssembler::toModel);
    }

    @Override
    @PatchMapping(value = "/admin/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<UserDto>>> makeTrainer(@PathVariable Long id) {
        return userService.makeTrainer(id)
                .flatMap(u -> pageableUserAssembler.getItemAssembler().toModel(u))
                .map(ResponseEntity::ok);
    }

    @Override
    @PutMapping(value = "/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<CustomEntityModel<UserDto>>> updateUser(@PathVariable Long id, @RequestPart(value = "files", required = false) Flux<FilePart> files,
                                                                       @RequestPart("body") String body,
                                                                       ServerWebExchange exchange) {
        return requestsUtils.getBodyFromJson(body, UserBody.class, objectMapper, threadPoolTaskScheduler).flatMap(
                userBody ->
                        userService.updateUser(id, userBody, requestsUtils.extractAuthUser(exchange), files)
                                .flatMap(u -> pageableUserAssembler.getItemAssembler().toModel(u))
                                .map(ResponseEntity::ok));
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping(value = "/exists/{userId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Void>> existsUserByIdAndRoleIn(@PathVariable Long userId, @RequestParam(required = false) Set<Role> roles) {
        return userService.existsUserByIdAndRoleIn(userId, roles)
                .map((p) -> ResponseEntity.noContent().build());
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/byIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<CustomEntityModel<UserDto>> getUsersByIdIn(@RequestParam(required = false) List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }
        return userService.getUsersByIdIn(ids)
                .flatMap(pageableUserAssembler.getItemAssembler()::toModel);
    }

    @Override
    @PostMapping(value = "/admin/email", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Void>> sendEmailAdmin(@Valid @RequestBody EmailRequest emailRequest) {
        return userService.sendEmailAdmin(emailRequest)
                .then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }

    @GetMapping(value = "/seedEmbeddings")
    public Mono<ResponseEntity<List<String>>> seedEmbeddings() {
        return userService.seedEmbeddings()
                .map(ResponseEntity::ok);
    }

}
