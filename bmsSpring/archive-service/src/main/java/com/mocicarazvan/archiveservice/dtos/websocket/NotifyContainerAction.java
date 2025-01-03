package com.mocicarazvan.archiveservice.dtos.websocket;


import com.mocicarazvan.archiveservice.dtos.enums.ContainerAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
public class NotifyContainerAction extends BaseWSDto {
    private ContainerAction action;
}
