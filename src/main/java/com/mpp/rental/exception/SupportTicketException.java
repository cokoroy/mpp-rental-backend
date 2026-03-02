package com.mpp.rental.exception;

/**
 * Exception thrown for support ticket business rule violations
 */
public class SupportTicketException extends RuntimeException {
    public SupportTicketException(String message) {
        super(message);
    }
}