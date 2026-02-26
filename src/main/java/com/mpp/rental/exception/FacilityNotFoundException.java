package com.mpp.rental.exception;

public class FacilityNotFoundException extends RuntimeException {
    public FacilityNotFoundException(String message) {
        super(message);
    }

    public FacilityNotFoundException(Integer facilityId) {
        super("Facility not found with ID: " + facilityId);
    }
}