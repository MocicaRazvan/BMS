package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.dtos.WithUserDtoImpl;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.models.ManyToOneUserImpl;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.beans.ManyToOneUserBeanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidIdsTest {

    @Mock
    private MC repository;

    private final ValidIds<ManyToOneUserImpl, MC, WithUserDtoImpl> validIds = new VIImpl();

    private final List<Long> ids = List.of(1L, 1L, 2L, 3L);
    private final Set<Long> idsSet = Set.of(1L, 2L, 3L);

    @Test
    void validIds_success() {
        when(repository.countByIds(eq(idsSet))).thenReturn(Flux.fromIterable(idsSet));
        StepVerifier.create(validIds.validIds(ids, repository, "test"))
                .expectComplete()
                .verify();
    }

    @Test
    void validIds_throwsIllegalAction() {
        when(repository.countByIds(eq(idsSet))).thenReturn(Flux.fromIterable(idsSet.stream().skip(0).limit(2).collect(Collectors.toSet())));

        StepVerifier.create(validIds.validIds(ids, repository, "test"))
                .expectErrorMatches(throwable -> throwable instanceof IllegalActionException)
                .verify();
    }

    @Test
    void validIdsWrapper_success() {
        when(repository.countByIds(eq(idsSet))).thenReturn(Flux.fromIterable(idsSet));

        StepVerifier.create(validIds.validIdsWrapper(ids, repository, "test"))
                .expectNextSequence(ids)
                .expectComplete()
                .verify();
    }

    @Test
    void validIdsWrapper_throwsIllegalAction() {
        when(repository.countByIds(eq(idsSet))).thenReturn(Flux.fromIterable(idsSet.stream().skip(0).limit(2).collect(Collectors.toSet())));

        StepVerifier.create(validIds.validIdsWrapper(ids, repository, "test"))
                .expectErrorMatches(throwable -> throwable instanceof IllegalActionException)
                .verify();
    }


    public interface MC extends ManyToOneUserBeanRepository, CountIds {

    }

    public static class VIImpl implements ValidIds<ManyToOneUserImpl, MC, WithUserDtoImpl> {

        @Override
        public Flux<WithUserDtoImpl> getModelsByIds(List<Long> ids) {
            return Flux.empty();
        }
    }
}