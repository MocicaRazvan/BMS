package com.mocicarazvan.templatemodule.exceptions.action;

import lombok.Getter;

@Getter
public class SubEntityNotOwner extends RuntimeException {
    private final Long authId;
    private final Long entityUserId;
    private final Long entityId;

    public SubEntityNotOwner(Long authId, Long entityUserId, Long entityId) {
        super("Entity user id is: " + entityUserId + " , but received user id: " + authId);
        this.authId = authId;
        this.entityUserId = entityUserId;
        this.entityId = entityId;

    }
}
