package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.enums.ConnectedStatus;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.repositories.generic.IdGeneratedRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface ConversationUserRepository extends IdGeneratedRepository<ConversationUser> {

    //    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<ConversationUser> findByEmail(String email);

    boolean existsByEmail(String email);

    //    @Lock(LockModeType.PESSIMISTIC_READ)
    List<ConversationUser> findAllByConnectedStatusIs(ConnectedStatus connectedStatus);
}
