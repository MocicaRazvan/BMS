package com.mocicarazvan.templatemodule.exceptions.action;

import com.mocicarazvan.templatemodule.exceptions.notFound.IdNameException;
import lombok.Getter;

@Getter
public class NotApprovedEntity extends IdNameException {
    public NotApprovedEntity(String name, Long id) {
        super(name, id, "Entity " + name + " with id " + id.toString() + " is not approved!");

    }
}
