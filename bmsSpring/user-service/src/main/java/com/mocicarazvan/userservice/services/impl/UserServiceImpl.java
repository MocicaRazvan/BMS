package com.mocicarazvan.userservice.services.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.email.EmailRequest;
import com.mocicarazvan.templatemodule.dtos.files.MetadataDto;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.email.EmailUtils;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.userservice.cache.redis.annotations.RedisReactiveRoleCache;
import com.mocicarazvan.userservice.cache.redis.annotations.RedisReactiveRoleCacheEvict;
import com.mocicarazvan.userservice.email.EmailTemplates;
import com.mocicarazvan.userservice.mappers.UserMapper;
import com.mocicarazvan.userservice.models.UserCustom;
import com.mocicarazvan.userservice.repositories.ExtendedUserRepository;
import com.mocicarazvan.userservice.repositories.UserRepository;
import com.mocicarazvan.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final List<String> allowedSortingFields = List.of("firstName", "lastName", "email", "createdAt", "updatedAt");

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final EntitiesUtils entitiesUtils;
    private final PageableUtilsCustom pageableUtilsCustom;
    private final FileClient fileClient;
    private final ObjectMapper objectMapper;
    private final ExtendedUserRepository extendedUserRepository;
    private final UserEmbedServiceImpl userEmbedService;
    private final TransactionalOperator transactionalOperator;
    private final static String USER_SERVICE_NAME = "userService";
    private final RabbitMqUpdateDeleteService<UserCustom> rabbitMqUpdateDeleteService;


    @Value("${front.url}")
    private String frontUrl;
    private final EmailUtils emailUtils;

    @Override
    public Mono<List<String>> seedEmbeddings() {
        return userRepository.findAll()
                .flatMap(user ->
                        userEmbedService.saveEmbedding(user.getId(), user.getEmail()).then(Mono.just("Seeded embeddings for user: " + user.getId())))
                .collectList()
                .as(transactionalOperator::transactional);
    }

    @Override
    @RedisReactiveCache(key = USER_SERVICE_NAME, id = "#id")
    public Mono<UserDto> getUser(Long id) {
        return
                entitiesUtils.getEntityById(id, "user", userRepository)
                        .map(userMapper::fromUserCustomToUserDto);
    }


    @RedisReactiveRoleCache(key = USER_SERVICE_NAME, idPath = "content.id", roleArgumentPath = "#roles!=null?#roles.isEmpty()?null:#roles[0]:null")
    @Override
    public Flux<PageableResponse<UserDto>> getAllUsers(PageableBody pageableBody, String email, Set<Role> roles,
                                                       Set<AuthProvider> providers, Boolean emailVerified, Boolean admin) {

        final String emailToSearch = email == null ? "" : email;

        Set<Role> finalRoles = handleEnum(roles, Role.class);
        Set<AuthProvider> finalProviders = handleEnum(providers, AuthProvider.class);


        return pageableUtilsCustom.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtilsCustom.createPageRequest(pageableBody))
                .flatMapMany(pr ->
                        pageableUtilsCustom.createPageableResponse(
                                extendedUserRepository.getUsersFiltered(pr, emailToSearch, finalRoles, finalProviders, emailVerified).map(userMapper::fromUserCustomToUserDto),
                                extendedUserRepository.countUsersFiltered(emailToSearch, finalRoles, finalProviders, emailVerified),
                                pr)


                );


    }

    private <T extends Enum<T>> Set<T> handleEnum(Set<T> items, Class<T> enumClass) {
        if (items == null) {
            items = EnumSet.noneOf(enumClass);
        }
        if (items.isEmpty()) {
            items.addAll(EnumSet.allOf(enumClass));
        }
        return items;
    }


    @Override
    @RedisReactiveRoleCacheEvict(key = USER_SERVICE_NAME, id = "#id", oldRole = Role.ROLE_USER, newRole = Role.ROLE_TRAINER)
    public Mono<UserDto> makeTrainer(Long id) {
        return
                entitiesUtils.getEntityById(id, "user", userRepository)
                        .flatMap(user -> {
                            if (user.getRole().equals(Role.ROLE_ADMIN)) {
                                return Mono.error(new IllegalActionException("User is admin!"));
                            } else if (user.getRole().equals(Role.ROLE_TRAINER)) {
                                return Mono.error(new IllegalActionException("User is trainer!"));
                            }
                            user.setRole(Role.ROLE_TRAINER);
                            return userRepository.save(user)
                                    .doOnSuccess(rabbitMqUpdateDeleteService::sendDeleteMessage)
                                    .map(userMapper::fromUserCustomToUserDto);
                        });
    }

    // todo test it
    @Override
    @RedisReactiveRoleCacheEvict(key = USER_SERVICE_NAME, id = "#id", oldRolePath = "role")
    public Mono<UserDto> updateUser(Long id, UserBody userBody, String userId, Flux<FilePart> files) {

        Flux<FilePart> finalFiles = files != null ? files : Flux.empty();


        return

                entitiesUtils.getEntityById(id, "user", userRepository)
                        .zipWith(getAuthUser(Long.parseLong(userId)))
                        .flatMap(tuple -> {
                            UserCustom user = tuple.getT1();
                            UserCustom authUser = tuple.getT2();
                            return Mono.zip(Mono.fromRunnable(() ->
                                            rabbitMqUpdateDeleteService.sendUpdateMessage(user.clone())
                                    ).thenReturn(true)
                                    , Mono.defer(() -> {
                                        if (!user.getId().equals(authUser.getId())) {
                                            return Mono.error(new PrivateRouteException());
                                        }
                                        user.setLastName(userBody.getLastName());
                                        user.setFirstName(userBody.getFirstName());

                                        if (user.getImage() != null) {
                                            return fileClient.deleteFiles(List.of(user.getImage())).thenReturn(user);
                                        }
                                        return Mono.just(user);
                                    })
                            ).map(Tuple2::getT2);
                        }).flatMap(user ->

                                finalFiles.hasElements().flatMap(ha -> {
                                            if (ha) {
                                                MetadataDto metadataDto = new MetadataDto();
                                                metadataDto.setFileType(FileType.IMAGE);
                                                metadataDto.setName("profile " + userId);
                                                metadataDto.setClientId(userId + "_profile");
                                                return fileClient.uploadFiles(finalFiles, metadataDto, objectMapper).map(fileUploadResponse -> {
                                                    user.setImage(fileUploadResponse.getFiles().get(0));
                                                    return user;
                                                });
                                            } else {
                                                user.setImage(null);
                                                return Mono.just(user);
                                            }

                                        }

                                )

                        ).flatMap(userRepository::save)
                        .map(userMapper::fromUserCustomToUserDto);

    }

    @Override
    public Mono<Boolean> existsUserByIdAndRoleIn(Long userId, Set<Role> roles) {
        return
                userRepository.existsByIdAndRoleIn(userId, roles)
                        .filter(Boolean::booleanValue)
                        .log()
                        .switchIfEmpty(Mono.error(new NotFoundEntity("user", userId)));
    }


    @Override
    @RedisReactiveCache(key = USER_SERVICE_NAME, idPath = "id")
    public Flux<UserDto> getUsersByIdIn(List<Long> ids) {
        return
                userRepository.findAllByIdIn(ids)
                        .map(userMapper::fromUserCustomToUserDto);
    }

    @Override
    public Mono<Void> sendEmailAdmin(EmailRequest emailRequest) {
        return emailUtils.sendEmail(emailRequest.getRecipientEmail(), emailRequest.getSubject(),
                EmailTemplates.adminTemplate(frontUrl, emailRequest.getContent()));
    }

    public Mono<UserCustom> getAuthUser(Long userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundEntity("user", userId)));
    }
}
