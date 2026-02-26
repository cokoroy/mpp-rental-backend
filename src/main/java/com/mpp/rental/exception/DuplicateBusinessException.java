package com.mpp.rental.exception;

public class DuplicateBusinessException extends RuntimeException {
    public DuplicateBusinessException(String message) {
        super(message);
    }
}
