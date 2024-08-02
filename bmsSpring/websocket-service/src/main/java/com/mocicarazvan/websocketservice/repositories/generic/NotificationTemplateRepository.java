package com.mocicarazvan.websocketservice.repositories.generic;


import com.mocicarazvan.websocketservice.models.Post;
import com.mocicarazvan.websocketservice.models.generic.IdGenerated;
import com.mocicarazvan.websocketservice.models.generic.NotificationTemplate;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface NotificationTemplateRepository<R extends IdGenerated, E extends Enum<E>,
        M extends NotificationTemplate<R, E>> extends JpaRepository<M, Long> {

    List<M> findAllByReferenceId(Long referenceId);

    List<M> findAllBySenderIdAndType(Long senderId, E type);

    List<M> findAllBySenderId(Long senderId);

    List<M> findAllByReceiverId(Long receiverId);

    List<M> findAllBySenderIdAndReceiverId(Long senderId, Long receiverId);

    List<M> findAllByReceiverIdAndType(Long receiverId, E type);


    void deleteAllBySenderId(Long senderId);

    void deleteAllBySenderIdAndType(Long senderId, E type);

    void deleteAllByReceiverId(Long receiverId);

    void deleteAllByReceiverIdAndType(Long receiverId, E type);

    //    @Lock(LockModeType.PESSIMISTIC_WRITE)
    void deleteAllBySenderIdAndReceiverIdAndType(Long senderId, Long receiverId, E type);

    //    @Lock(LockModeType.PESSIMISTIC_WRITE)
    void deleteAllBySenderIdAndReceiverId(Long senderId, Long receiverId);

    //    @Lock(LockModeType.PESSIMISTIC_WRITE)
    void deleteAllByReferenceId(Long referenceId);


}
