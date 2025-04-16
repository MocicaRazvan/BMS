package com.mocicarazvan.templatemodule.utils;

import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.NotApprovedEntity;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityNotOwner;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.models.ApproveImpl;
import com.mocicarazvan.templatemodule.models.ManyToOneUserImpl;
import com.mocicarazvan.templatemodule.models.TitleBodyImpl;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class EntitiesUtilsTest {

    @Mock
    private MTC repository;

    @InjectMocks
    private EntitiesUtils entitiesUtils;

    @Test
    void validIds_valid() {
        var ids = List.of(1L, 2L, 3L);
        when(repository.countByIds(ids)).thenReturn(Flux.just((long) ids.size()));
        StepVerifier.create(entitiesUtils.validIds(ids, repository, "dummy"))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void validIds_listEmpty() {
        var ids = new ArrayList<Long>();
        when(repository.countByIds(ids)).thenReturn(Flux.just((long) ids.size()));
        StepVerifier.create(entitiesUtils.validIds(ids, repository, "dummy"))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void validIds_shouldThrow() {
        var ids = List.of(1L, 2L, 3L);
        when(repository.countByIds(ids)).thenReturn(Flux.just(0L));
        StepVerifier.create(entitiesUtils.validIds(ids, repository, "dummy"))
                .expectErrorMatches(e -> e instanceof IllegalActionException
                        && e.getMessage().equals("dummy " + ids + " are not valid"))
                .verify();
    }

    @Test
    void setReaction_likeUserNotInLikes() {
        var model = TitleBodyImpl.builder()
                .userLikes(List.of(15L))
                .userDislikes(List.of(10L))
                .build();
        var user = UserDto.builder().id(1L).build();

        StepVerifier.create(entitiesUtils.setReaction(model, user, "like"))
                .expectNextMatches(e -> e.getUserLikes().contains(user.getId()))
                .verifyComplete();
    }

    @Test
    void setReaction_likeUserInLikes() {
        var model = TitleBodyImpl.builder()
                .userLikes(List.of(15L, 1L))
                .userDislikes(List.of(10L))
                .build();
        var user = UserDto.builder().id(1L).build();

        StepVerifier.create(entitiesUtils.setReaction(model, user, "like"))
                .expectNextMatches(e -> !e.getUserLikes().contains(user.getId()))
                .verifyComplete();
    }

    @Test
    void setReaction_dislikeUserNotInLikes() {
        var model = TitleBodyImpl.builder()
                .userLikes(List.of(15L))
                .userDislikes(List.of(10L))
                .build();
        var user = UserDto.builder().id(1L).build();

        StepVerifier.create(entitiesUtils.setReaction(model, user, "dislike"))
                .expectNextMatches(e -> e.getUserDislikes().contains(user.getId()))
                .verifyComplete();
    }

    @Test
    void setReaction_dislikeUserInLikes() {
        var model = TitleBodyImpl.builder()
                .userLikes(List.of(15L))
                .userDislikes(List.of(10L, 1L))
                .build();
        var user = UserDto.builder().id(1L).build();

        StepVerifier.create(entitiesUtils.setReaction(model, user, "dislike"))
                .expectNextMatches(e -> !e.getUserDislikes().contains(user.getId()))
                .verifyComplete();
    }

    @Test
    void setReaction_invalidOption() {
        var model = TitleBodyImpl.builder()
                .userLikes(List.of(15L))
                .userDislikes(List.of(10L))
                .build();
        var user = UserDto.builder().id(1L).build();

        StepVerifier.create(entitiesUtils.setReaction(model, user, "inv"))
                .expectNextMatches(e ->
                        e.getUserLikes().equals(List.of(15L)) && e.getUserDislikes().equals(List.of(10L))
                )
                .verifyComplete();
    }

    @Test
    void getEntityById_found() {
        var model = new ManyToOneUserImpl();
        when(repository.findById(anyLong())).thenReturn(Mono.just(model));
        StepVerifier.create(entitiesUtils.getEntityById(1L, "dummy", repository))
                .expectNext(model)
                .verifyComplete();
    }

    @Test
    void getEntityById_notFound() {
        var ex = new NotFoundEntity("dummy", 1L);
        when(repository.findById(anyLong())).thenReturn(Mono.empty());
        StepVerifier.create(entitiesUtils.getEntityById(1L, "dummy", repository))
                .expectErrorMatches(
                        e -> e instanceof NotFoundEntity
                                && e.getMessage().equals(ex.getMessage())
                                && ((NotFoundEntity) e).getId().equals(ex.getId())
                )
                .verify();
    }

    @Test
    void checkEntitySubOwner() {
        var model = ManyToOneUserImpl.builder().id(1L).userId(1L).build();
        var user = UserDto.builder().id(1L).build();
        StepVerifier.create(entitiesUtils.checkSubEntityOwner(model, user))
                .verifyComplete();
    }

    @Test
    void checkEntitySubOwner_notOwner() {
        var model = ManyToOneUserImpl.builder().id(1L).userId(11L).build();
        var user = UserDto.builder().id(1L).build();
        var ex = new SubEntityNotOwner(user.getId(), model.getUserId(), model.getId());
        StepVerifier.create(entitiesUtils.checkSubEntityOwner(model, user))
                .expectErrorMatches(
                        e -> e instanceof SubEntityNotOwner
                                && e.getMessage().equals(ex.getMessage())
                                && ((SubEntityNotOwner) e).getAuthId().equals(ex.getAuthId())
                                && ((SubEntityNotOwner) e).getEntityUserId().equals(ex.getEntityUserId())
                                && ((SubEntityNotOwner) e).getEntityId().equals(ex.getEntityId())
                )
                .verify();
    }

    @Test
    void checkOwner() {
        var model = ManyToOneUserImpl.builder().id(1L).userId(1L).build();
        String userId = "1";
        StepVerifier.create(entitiesUtils.checkOwner(model, userId))
                .verifyComplete();
    }

    @Test
    void checkOwner_notOwner() {
        var model = ManyToOneUserImpl.builder().id(1L).userId(1L).build();
        String userId = "2";
        StepVerifier.create(entitiesUtils.checkOwner(model, userId))
                .expectError(PrivateRouteException.class)
                .verify();
    }

    @Test
    void checkEntityOwnerOrAdmin_owner() {
        var model = ManyToOneUserImpl.builder().id(1L).userId(1L).build();
        var user = UserDto.builder().id(1L).role(Role.ROLE_TRAINER).build();
        StepVerifier.create(entitiesUtils.checkEntityOwnerOrAdmin(model, user))
                .verifyComplete();
    }

    @Test
    void checkEntityOwnerOrAdmin_admin() {
        var model = ManyToOneUserImpl.builder().id(1L).userId(1L).build();
        var user = UserDto.builder().id(12L).role(Role.ROLE_ADMIN).build();
        StepVerifier.create(entitiesUtils.checkEntityOwnerOrAdmin(model, user))
                .verifyComplete();
    }

    @Test
    void checkEntityOwnerOrAdmin_noOwnerOrAdmin() {
        var model = ManyToOneUserImpl.builder().id(1L).userId(1L).build();
        var user = UserDto.builder().id(12L).role(Role.ROLE_TRAINER).build();
        var ex = new SubEntityNotOwner(user.getId(), model.getUserId(), model.getId());
        StepVerifier.create(entitiesUtils.checkEntityOwnerOrAdmin(model, user))
                .expectErrorMatches(
                        e -> e instanceof SubEntityNotOwner
                                && e.getMessage().equals(ex.getMessage())
                                && ((SubEntityNotOwner) e).getAuthId().equals(ex.getAuthId())
                                && ((SubEntityNotOwner) e).getEntityUserId().equals(ex.getEntityUserId())
                                && ((SubEntityNotOwner) e).getEntityId().equals(ex.getEntityId())
                )
                .verify();
    }

    @Test
    void checkAdmin() {
        var user = UserDto.builder().id(1L).role(Role.ROLE_ADMIN).build();
        StepVerifier.create(entitiesUtils.checkAdmin(user))
                .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ROLE_TRAINER", "ROLE_USER"})
    void checkAdmin_notAdmin(Role role) {
        var user = UserDto.builder().id(1L).role(role).build();
        StepVerifier.create(entitiesUtils.checkAdmin(user))
                .expectErrorMatches(
                        e -> e instanceof IllegalActionException
                                && e.getMessage().equals("User is not admin")
                )
                .verify();
    }

    @Test
    void checkApproved() {
        var model = ApproveImpl.builder().id(1L).approved(true).build();
        StepVerifier.create(entitiesUtils.checkApproved(model, "approve"))
                .verifyComplete();
    }

    @Test
    void checkApproved_notApproved() {
        var model = ApproveImpl.builder().id(1L).approved(false).build();
        var ex = new NotApprovedEntity("approve", model.getId());
        StepVerifier.create(entitiesUtils.checkApproved(model, "approve"))
                .expectErrorMatches(
                        e -> e instanceof NotApprovedEntity
                                && e.getMessage().equals(ex.getMessage())
                                && ((NotApprovedEntity) e).getId().equals(ex.getId())
                )
                .verify();
    }

    @Test
    void convertArrayToList_arrayNotNull() {
        Long[] array = {1L, 2L, 3L};
        List<Long> list = EntitiesUtils.convertArrayToList(array);
        assertEquals(3, list.size());
        assertEquals(1L, list.get(0));
        assertEquals(2L, list.get(1));
        assertEquals(3L, list.get(2));
    }

    @Test
    void convertArrayToList_arrayNull() {
        Long[] array = null;
        List<Long> list = EntitiesUtils.convertArrayToList(array);
        assertEquals(0, list.size());
    }

    @Test
    void camToSnakeCase_valid() {
        String field = "fieldName";
        String result = EntitiesUtils.camelToSnake(field);
        assertEquals("field_name", result);
    }

    @Test
    void camToSnakeCase_empty() {
        String field = "";
        String result = EntitiesUtils.camelToSnake(field);
        assertEquals("", result);
    }

    @Test
    void testGetListOfNotNullObjects_withNulls() {
        List<Object> result = EntitiesUtils.getListOfNotNullObjects("a", null, 123, null, "b");

        assertEquals(3, result.size());
        assertEquals(List.of("a", 123, "b"), result);
    }

    @Test
    void testGetListOfNotNullObjects_allNonNull() {
        List<Object> result = EntitiesUtils.getListOfNotNullObjects("x", 1, true);

        assertEquals(3, result.size());
        assertEquals(List.of("x", 1, true), result);
    }

    @Test
    void testGetListOfNotNullObjects_allNulls() {
        List<Object> result = EntitiesUtils.getListOfNotNullObjects(null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetListOfNotNullObjects_emptyInput() {
        List<Object> result = EntitiesUtils.getListOfNotNullObjects();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    public interface MTC extends ManyToOneUserRepository<ManyToOneUserImpl>, CountIds {
    }

}