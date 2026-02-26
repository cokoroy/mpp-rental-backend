package com.mpp.rental.exception;

public class DuplicateEventException extends RuntimeException {
    public DuplicateEventException(String eventName) {
        super("Event with name '" + eventName + "' already exists");
    }

    public DuplicateEventException(String message, boolean customMessage) {
        super(message);
    }
}
