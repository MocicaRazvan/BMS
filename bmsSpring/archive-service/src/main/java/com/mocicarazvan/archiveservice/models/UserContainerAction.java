package com.mocicarazvan.archiveservice.models;


import com.mocicarazvan.archiveservice.models.keys.UserContainerActionKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Table("user_container_action")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserContainerAction extends UserContainerActionKey implements Persistable<UserContainerActionKey> {
    private LocalDateTime timestamp;

    @Override
    public UserContainerActionKey getId() {
        return UserContainerActionKey.builder()
                .actionId(getActionId())
                .userId(getUserId())
                .build();
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
