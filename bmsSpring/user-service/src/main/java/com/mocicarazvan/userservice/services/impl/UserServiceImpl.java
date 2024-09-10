package com.mocicarazvan.userservice.services.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.userservice.cache.FilteredCacheListCaffeineRoleUserFilterKey;
import com.mocicarazvan.userservice.email.EmailTemplates;
import com.mocicarazvan.userservice.mappers.UserMapper;
import com.mocicarazvan.userservice.models.UserCustom;
import com.mocicarazvan.userservice.repositories.UserRepository;
import com.mocicarazvan.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final List<String> allowedSortingFields = List.of("firstName", "lastName", "email");

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final EntitiesUtils entitiesUtils;
    private final PageableUtilsCustom pageableUtilsCustom;
    private final FileClient fileClient;
    private final ObjectMapper objectMapper;
    protected final FilteredCacheListCaffeineRoleUserFilterKey cacheHandler;


    @Value("${front.url}")
    private String frontUrl;
    private final EmailUtils emailUtils;

    @Override
    public Mono<UserDto> getUser(Long id) {
        return
                cacheHandler.getUserPersist(
                        entitiesUtils.getEntityById(id, "user", userRepository)
                                .map(userMapper::fromUserCustomToUserDto), id);
    }

    @Override
    public Flux<PageableResponse<UserDto>> getAllUsers(PageableBody pageableBody, String email, Set<Role> roles,
                                                       Set<AuthProvider> providers, Boolean emailVerified, Boolean admin) {

        final String emailToSearch = email == null ? "" : email;

        Set<Role> finalRoles = handleEnum(roles, Role.class);
        Set<AuthProvider> finalProviders = handleEnum(providers, AuthProvider.class);


        return pageableUtilsCustom.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtilsCustom.createPageRequest(pageableBody))
                .flatMapMany(pr ->

                        cacheHandler.getAllUsersPersist(
                                pageableUtilsCustom.createPageableResponse(
                                        (emailVerified == null ? userRepository.findAllByEmailContainingIgnoreCaseAndRoleInAndProviderIn(emailToSearch, finalRoles,
                                                finalProviders, pr) :
                                                userRepository.findAllByEmailContainingIgnoreCaseAndRoleInAndProviderInAndEmailVerifiedIs(emailToSearch, finalRoles,
                                                        finalProviders, emailVerified, pr))
                                                .log()
                                                .map(userMapper::fromUserCustomToUserDto),
                                        (emailVerified == null ? userRepository.countAllByEmailContainingIgnoreCaseAndRoleInAndProviderIn(emailToSearch, finalRoles, finalProviders)
                                                : userRepository.countAllByEmailContainingIgnoreCaseAndRoleInAndProviderInAndEmailVerifiedIs(emailToSearch, finalRoles, finalProviders, emailVerified)
                                        ),


                                        pr), pageableBody, emailToSearch, finalRoles, finalProviders, emailVerified, admin)


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
    public Mono<UserDto> makeTrainer(Long id) {
        return
                cacheHandler.makeTrainerInvalidate(
                        entitiesUtils.getEntityById(id, "user", userRepository)
                                .flatMap(user -> {
                                    if (user.getRole().equals(Role.ROLE_ADMIN)) {
                                        return Mono.error(new IllegalActionException("User is admin!"));
                                    } else if (user.getRole().equals(Role.ROLE_TRAINER)) {
                                        return Mono.error(new IllegalActionException("User is trainer!"));
                                    }
                                    user.setRole(Role.ROLE_TRAINER);
                                    return userRepository.save(user).map(userMapper::fromUserCustomToUserDto);
                                }), id);
    }

    // todo test it
    @Override
    public Mono<UserDto> updateUser(Long id, UserBody userBody, String userId, Flux<FilePart> files) {

        Flux<FilePart> finalFiles = files != null ? files : Flux.empty();


        return
                cacheHandler.updateUserInvalidate(
                        entitiesUtils.getEntityById(id, "user", userRepository)
                                .zipWith(getAuthUser(Long.parseLong(userId)))
                                .flatMap(tuple -> {
                                    UserCustom user = tuple.getT1();
                                    UserCustom authUser = tuple.getT2();

                                    if (!user.getId().equals(authUser.getId())) {
                                        return Mono.error(new PrivateRouteException());
                                    }
                                    user.setLastName(userBody.getLastName());
                                    user.setFirstName(userBody.getFirstName());

                                    if (user.getImage() != null) {
                                        return fileClient.deleteFiles(List.of(user.getImage())).thenReturn(user);
                                    }
                                    return Mono.just(user);
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

                                ).flatMap(userRepository::save).map(userMapper::fromUserCustomToUserDto), id);

    }

    @Override
    public Mono<Boolean> existsUserByIdAndRoleIn(Long userId, Set<Role> roles) {
        return
                cacheHandler.existsUserByIdAndRoleInPersist(
                        userRepository.existsByIdAndRoleIn(userId, roles)
                                .filter(Boolean::booleanValue)
                                .log()
                                .switchIfEmpty(Mono.error(new NotFoundEntity("user", userId))), userId, roles);
    }

    @Override
    public Flux<UserDto> getUsersByIdIn(List<Long> ids) {
        return
                cacheHandler.getUsersByIdInPersist(
                        userRepository.findAllByIdIn(ids)
                                .map(userMapper::fromUserCustomToUserDto), ids);
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
