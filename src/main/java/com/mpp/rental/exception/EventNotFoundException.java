package com.mpp.rental.exception;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(Integer eventId) {
        super("Event not found with ID: " + eventId);
    }

    public EventNotFoundException(String message) {
        super(message);
    }
}
