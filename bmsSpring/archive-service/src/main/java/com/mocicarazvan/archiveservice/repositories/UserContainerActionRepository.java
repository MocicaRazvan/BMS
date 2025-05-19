package com.mocicarazvan.archiveservice.repositories;

import com.mocicarazvan.archiveservice.models.NotifyContainerModel;
import com.mocicarazvan.archiveservice.models.UserContainerAction;
import com.mocicarazvan.archiveservice.models.keys.UserContainerActionKey;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface UserContainerActionRepository extends R2dbcRepository<UserContainerAction, UserContainerActionKey> {


    @Query("""
             select nc.* from notify_container nc
             join user_container_action uc
             on nc.id=uc.action_id
             where uc.user_id = :userId
             order by nc.timestamp desc
            """)
    Flux<NotifyContainerModel> findAllNotificationsByUserId(String userId);

    Mono<Void> deleteAllByUserIdAndActionIdIn(String userId, Collection<String> actionIds);
}
