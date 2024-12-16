package com.mocicarazvan.userservice.services;


import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.email.EmailRequest;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface UserService {
    Mono<List<String>> seedEmbeddings();

    Mono<UserDto> getUser(Long id);

    Flux<PageableResponse<UserDto>> getAllUsers(PageableBody pageableBody, String email, Set<Role> roles, Set<AuthProvider> providers, Boolean emailVerified, Boolean admin);

    Mono<UserDto> makeTrainer(Long id);

    Mono<UserDto> updateUser(Long id, UserBody userBody, String userId, Flux<FilePart> files);

    Mono<Boolean> existsUserByIdAndRoleIn(Long userId, Set<Role> roles);

    Flux<UserDto> getUsersByIdIn(List<Long> ids);

    Mono<Void> sendEmailAdmin(EmailRequest emailRequest);
}
