package com.mocicarazvan.archiveservice.services;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Getter
public class SaveMessagesAggregator {

    private final SaveBatchMessages saveBatchMessages;
    private final BatchNotify batchNotify;

}
