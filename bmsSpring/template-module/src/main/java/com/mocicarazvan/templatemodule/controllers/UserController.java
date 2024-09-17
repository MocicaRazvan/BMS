package com.mocicarazvan.templatemodule.controllers;


import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.email.EmailRequest;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface UserController {

    Mono<ResponseEntity<List<Role>>> getRoles();

    Mono<ResponseEntity<CustomEntityModel<UserDto>>> getUser(Long id);

    Flux<PageableResponse<CustomEntityModel<UserDto>>> getAllUsers(
            @Valid @RequestBody PageableBody pageableBody, @RequestParam(required = false) String email,
            @RequestParam(required = false) Set<Role> roles, @RequestParam(required = false) Set<AuthProvider> providers, @RequestParam(required = false) Boolean emailVerified,
            @RequestParam(name = "admin", required = false, defaultValue = "false") Boolean admin

    );

    Mono<ResponseEntity<CustomEntityModel<UserDto>>> makeTrainer(Long id);

    Mono<ResponseEntity<CustomEntityModel<UserDto>>> updateUser(Long id,
                                                                @RequestPart(value = "files", required = false) Flux<FilePart> files,
                                                                @RequestPart("body") String body,
                                                                ServerWebExchange exchange);

    Mono<ResponseEntity<Void>> existsUserByIdAndRoleIn(Long userId, @RequestParam Set<Role> roles);

    Flux<CustomEntityModel<UserDto>> getUsersByIdIn(@RequestParam List<Long> ids);

    Mono<ResponseEntity<Void>> sendEmailAdmin(@Valid @RequestBody EmailRequest emailRequest);
}
