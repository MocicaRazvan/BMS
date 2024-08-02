package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ValidIds<M extends ManyToOneUser, R extends ManyToOneUserRepository<M> & CountIds, RESP extends WithUserDto> {

    //    Mono<Void> validIds(List<Long> ids);
    default Mono<Void> validIds(List<Long> ids, R modelRepository, String name) {
        return modelRepository.countByIds(ids)
                .map(count -> count == ids.size())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new IllegalActionException(name + " " + ids.toString() + " are not valid")))
                .then();
    }

    Flux<RESP> getModelsByIds(List<Long> ids);

}
