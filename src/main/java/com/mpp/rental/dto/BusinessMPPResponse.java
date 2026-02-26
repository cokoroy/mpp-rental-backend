package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for MPP Business Management View
 * Extends basic business information with owner details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessMPPResponse {

    // Business Information
    private Long businessId;
    private String businessName;
    private String ssmNumber;
    private String businessCategory;
    private String businessDesc;
    private String businessStatus;
    private LocalDateTime businessRegisteredAt;
    private String ssmDocument;

    // Owner Information (for MPP view only)
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhoneNumber;
    private String ownerCategory; // MPP, STUDENT, NON_STUDENT
    private String ownerStatus; // PENDING, ACTIVE, BLOCKED
}