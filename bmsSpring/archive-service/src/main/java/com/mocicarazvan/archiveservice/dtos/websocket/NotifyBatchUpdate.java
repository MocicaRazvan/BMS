package com.mocicarazvan.archiveservice.dtos.websocket;


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
public class NotifyBatchUpdate extends BaseWSDto {

    private long numberProcessed;
    private boolean finished;

}
