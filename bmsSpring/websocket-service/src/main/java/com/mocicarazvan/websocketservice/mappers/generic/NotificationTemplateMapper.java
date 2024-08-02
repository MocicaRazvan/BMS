package com.mocicarazvan.websocketservice.mappers.generic;

import com.mocicarazvan.websocketservice.dtos.generic.IdResponse;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateResponse;
import com.mocicarazvan.websocketservice.models.generic.IdGenerated;
import com.mocicarazvan.websocketservice.models.generic.NotificationTemplate;


public abstract class NotificationTemplateMapper<R extends IdGenerated, RRESP extends IdResponse, E extends Enum<E>,
        MODEL extends NotificationTemplate<R, E>, RESPONSE extends NotificationTemplateResponse<RRESP, E>
        > {


    public abstract RESPONSE fromModelToResponse(MODEL model);

}
