package com.mpp.rental.exception;

public class DuplicateFacilityException extends RuntimeException {
//    public DuplicateFacilityException(String message) {
//        super(message);
//    }

    public DuplicateFacilityException(String facilityName) {
        super("Facility with name '" + facilityName + "' already exists");
    }
}