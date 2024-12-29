package com.mocicarazvan.archiveservice.exceptions;

public class QueueNameNotValid extends RuntimeException {
    private String name;

    public QueueNameNotValid(String message) {
        super(
                String.format("The queue name %s is not valid.", message)
        );
    }
}
