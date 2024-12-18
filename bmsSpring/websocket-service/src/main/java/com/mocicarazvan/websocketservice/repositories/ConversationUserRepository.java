package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.enums.ConnectedStatus;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.repositories.generic.IdGeneratedRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationUserRepository extends IdGeneratedRepository<ConversationUser> {

    Optional<ConversationUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<ConversationUser> findAllByConnectedStatusIs(ConnectedStatus connectedStatus);
}
