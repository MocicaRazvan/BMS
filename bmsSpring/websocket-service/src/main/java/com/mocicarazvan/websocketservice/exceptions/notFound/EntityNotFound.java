package com.mocicarazvan.websocketservice.exceptions.notFound;

public class EntityNotFound extends NotFoundBase {

    public EntityNotFound(String name, Long id) {
        super(name + " with id " + id + " not found");
    }
}
