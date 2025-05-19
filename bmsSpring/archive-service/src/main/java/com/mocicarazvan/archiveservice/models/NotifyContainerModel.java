package com.mocicarazvan.archiveservice.models;


import com.mocicarazvan.archiveservice.dtos.enums.ContainerAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "notify_container")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifyContainerModel implements Persistable<String> {
    @Id
    private String id;
    private String queueName;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private ContainerAction action;

    @Override
    public boolean isNew() {
        return id != null;
    }


}
