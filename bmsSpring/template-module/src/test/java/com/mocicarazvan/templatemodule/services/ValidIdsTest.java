package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.dtos.WithUserDtoImpl;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.models.ManyToOneUserImpl;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.beans.ManyToOneUserBeanRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidIdsTest {

    @Mock
    private MC repository;

    private final ValidIds<ManyToOneUserImpl, MC, WithUserDtoImpl> validIds = new VIImpl();

    private static final List<Long> ids = List.of(1L, 1L, 2L, 3L);
    private static final Set<Long> idsSet = Set.of(1L, 2L, 3L);

    static Stream<Arguments> provideListAndSet() {
        return Stream.of(
                Arguments.of(ids),
                Arguments.of(idsSet)
        );
    }

    @ParameterizedTest
    @MethodSource("provideListAndSet")
    void validIds_success(Collection<Long> curIds) {
        when(repository.countByIds(anySet())).thenAnswer(invocation -> Mono.just((long) invocation.getArgument(0, Set.class).size()));
        StepVerifier.create(validIds.validIds(curIds, repository, "test"))
                .expectComplete()
                .verify();
    }

    @ParameterizedTest
    @MethodSource("provideListAndSet")
    void validIds_throwsIllegalAction(Collection<Long> curIds) {
        when(repository.countByIds(anySet())).thenAnswer(invocation -> Mono.just((long) invocation.getArgument(0, Set.class).size() - 1));

        StepVerifier.create(validIds.validIds(curIds, repository, "test"))
                .expectErrorMatches(throwable -> throwable instanceof IllegalActionException)
                .verify();
    }

    @ParameterizedTest
    @MethodSource("provideListAndSet")
    void validIdsWrapper_success(Collection<Long> curIds) {
        when(repository.countByIds(anySet())).thenAnswer(invocation -> Mono.just((long) invocation.getArgument(0, Set.class).size()));

        StepVerifier.create(validIds.validIdsWrapper(curIds, repository, "test"))
                .expectNextSequence(curIds)
                .expectComplete()
                .verify();
    }

    @ParameterizedTest
    @MethodSource("provideListAndSet")
    void validIdsWrapper_throwsIllegalAction(Collection<Long> curIds) {
        when(repository.countByIds(anySet())).thenAnswer(invocation -> Mono.just((long) invocation.getArgument(0, Set.class).size() - 1));

        StepVerifier.create(validIds.validIdsWrapper(curIds, repository, "test"))
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