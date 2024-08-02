package com.mocicarazvan.templatemodule.exceptions.notFound;


import lombok.Getter;

@Getter
public class NotFoundEntity extends IdNameException {


    public NotFoundEntity(String name, Long id) {
        super(name, id, "Entity " + name + " with id " + id.toString() + " was not found!");

    }
}
