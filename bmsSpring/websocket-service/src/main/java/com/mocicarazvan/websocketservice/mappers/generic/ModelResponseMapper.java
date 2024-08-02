package com.mocicarazvan.websocketservice.mappers.generic;

public interface ModelResponseMapper<MODEL, RESPONSE> {
    RESPONSE fromModelToResponse(MODEL model);
}
