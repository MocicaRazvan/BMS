package com.mocicarazvan.templatemodule.exceptions.common;


import lombok.Getter;

@Getter
public class ServiceCallFailedException extends RuntimeException {

    public String serviceName;
    public String servicePath;

    public ServiceCallFailedException(String message, String serviceName, String servicePath) {
        super(message);
        this.serviceName = serviceName;
        this.servicePath = servicePath;
    }


}
