package com.mocicarazvan.templatemodule.utils;

import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.NotApprovedEntity;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityNotOwner;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.models.Approve;
import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import com.mocicarazvan.templatemodule.models.TitleBody;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class EntitiesUtils {

    public <M extends ManyToOneUser, R extends ManyToOneUserRepository<M> & CountIds> Mono<Void> validIds(List<Long> ids, R modelRepository, String name) {
        return modelRepository.countByIds(ids)
                .map(count -> count == ids.size())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new IllegalActionException(name + " " + ids.toString() + " are not valid")))
                .then();
    }

    public <M extends TitleBody> Mono<M> setReaction(M model, UserDto user, String type) {
        Set<Long> likes = new HashSet<>(model.getUserLikes());
        Set<Long> dislikes = new HashSet<>(model.getUserDislikes());

        if (type.equals("like")) {
            if (likes.contains(user.getId())) {
                likes.remove(user.getId());
            } else {
                likes.add(user.getId());
                dislikes.remove(user.getId());
            }
        } else if (type.equals("dislike")) {
            if (dislikes.contains(user.getId())) {
                dislikes.remove(user.getId());
            } else {
                dislikes.add(user.getId());
                likes.remove(user.getId());
            }
        }

        model.setUserLikes(likes.stream().toList());
        model.setUserDislikes(dislikes.stream().toList());
        return Mono.just(model);
    }

    public <T> Mono<T> getEntityById(Long id, String name, R2dbcRepository<T, Long> repository) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundEntity(name, id)));
    }


    public Mono<Void> checkSubEntityOwner(ManyToOneUser sub, UserDto user) {
        if (!sub.getUserId().equals(user.getId())) {
            return Mono.error(new SubEntityNotOwner(user.getId(), sub.getUserId(), sub.getId()));
        }
        return Mono.empty();
    }

    public Mono<Void> checkOwner(ManyToOneUser e, String userId) {
        if (!e.getUserId().equals(Long.valueOf(userId))) {
            return Mono.error(new PrivateRouteException());
        }
        return Mono.empty();
    }

    public Mono<Void> checkEntityOwnerOrAdmin(ManyToOneUser entity, UserDto user) {
        if (!entity.getUserId().equals(user.getId()) && !user.getRole().equals(Role.ROLE_ADMIN)) {
            return Mono.error(new SubEntityNotOwner(user.getId(), entity.getUserId(), entity.getId()));
        }
        return Mono.empty();
    }

    public Mono<Void> checkAdmin(UserDto user) {
        if (!user.getRole().equals(Role.ROLE_ADMIN)) {
            return Mono.error(new IllegalActionException("User is not admin"));
        }
        return Mono.empty();
    }

    public Mono<Void> checkApproved(Approve entity, String name) {
        if (!entity.isApproved()) {
            return Mono.error(new NotApprovedEntity(name, entity.getId()));
        }
        return Mono.empty();
    }

    public static <T> List<T> convertArrayToList(T[] array) {
        return array != null ? List.of(array) : List.of();
    }

    public static String camelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    public static Double getDoubleValue(Row row, String columnName) {
        Double value = row.get(columnName, Double.class);
        return value != null ? value : 0.0;
    }
}
