package com.mocicarazvan.archiveservice.exceptions;


import lombok.Getter;

@Getter
public class QueuePrefixNotFound extends RuntimeException {
    private String name;

    public QueuePrefixNotFound(String message) {
        super(
                String.format("Queue prefix not found: %s", message)
        );
    }
}
