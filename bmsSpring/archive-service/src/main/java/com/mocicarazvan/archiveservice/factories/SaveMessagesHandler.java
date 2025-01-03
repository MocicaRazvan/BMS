package com.mocicarazvan.archiveservice.factories;


import com.mocicarazvan.archiveservice.services.BatchNotify;
import com.mocicarazvan.archiveservice.services.SaveBatchMessages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Getter
public class SaveMessagesHandler {

    private final SaveBatchMessages saveBatchMessages;
    private final BatchNotify batchNotify;

}
