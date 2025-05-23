package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface ValidIds<M extends ManyToOneUser, R extends ManyToOneUserRepository<M> & CountIds, RESP extends WithUserDto> {

    default Mono<Void> validIds(Collection<Long> ids, R modelRepository, String name) {
        return
                Flux.fromIterable(ids)
                        .collect(Collectors.toSet())
                        .flatMap(uIds ->
                                modelRepository.countByIds(
                                                uIds
                                        )
                                        .map(cIds -> {
//                                            if (cIds.size() != ids.size()) {
//                                                return false;
//                                            }
//                                            Collections.sort(ids);
//                                            Collections.sort(cIds);
//                                            return ids.equals(cIds);
                                            return cIds == uIds.size();
                                        })
                                        .filter(Boolean::booleanValue)
                                        .switchIfEmpty(Mono.error(new IllegalActionException(name + " " + ids.toString() + " are not valid")))
//                .log()
                                        .then());
    }

    default Flux<Long> validIdsWrapper(Collection<Long> ids, R modelRepository, String name) {
        return validIds(ids, modelRepository, name)
                .thenMany(Flux.defer(() -> Flux.fromIterable(ids)));
    }

    Flux<RESP> getModelsByIds(List<Long> ids);

}
